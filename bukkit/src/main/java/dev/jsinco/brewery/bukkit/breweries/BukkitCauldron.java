package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.brew.*;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.event.ListenerUtil;
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult;
import dev.jsinco.brewery.bukkit.util.*;
import dev.jsinco.brewery.bukkit.adapter.BukkitAdapter;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.sound.SoundDefinition;
import dev.jsinco.brewery.util.MessageUtil;
import dev.jsinco.brewery.util.Registry;
import dev.jsinco.brewery.vector.BreweryLocation;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BukkitCauldron implements dev.jsinco.brewery.breweries.Cauldron {

    private static final Random RANDOM = new Random();

    private final BreweryLocation location;
    @Getter
    @Setter
    private boolean hot = false;
    @Getter
    private Brew brew;
    private static final Color LAVA_COLOR = Color.fromRGB(Integer.parseInt("d45a12", 16));
    private static final Color WATER_COLOR = Color.AQUA;
    private static final Color SNOW_COLOR = Color.fromRGB(Integer.parseInt("f8fdfd", 16));
    private boolean brewExtracted = false;
    private Color particleColor = Color.AQUA;
    private @Nullable Recipe<ItemStack> recipe;


    public BukkitCauldron(BreweryLocation location, boolean hot) {
        this.location = location;
        this.hot = hot;
        this.brew = new BrewImpl(List.of());
    }

    public BukkitCauldron(Brew brew, BreweryLocation location) {
        this.location = location;
        this.brew = brew;
    }

    private static CauldronType findCauldronType(Block block) {
        return Registry.CAULDRON_TYPE.values()
                .stream()
                .filter(cauldronType -> block.getType().getKey().toString().equals(cauldronType.materialKey()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Expected cauldron"));
    }

    @Override
    public void tick() {
        if (!BlockUtil.isChunkLoaded(location)) {
            return;
        }
        if (!Tag.CAULDRONS.isTagged(getBlock().getType()) || getBlock().getType() == Material.CAULDRON) {
            ListenerUtil.removeActiveSinglePositionStructure(this, TheBrewingProject.getInstance().getBreweryRegistry(), TheBrewingProject.getInstance().getDatabase());
            return;
        }
        this.hot = isHeatSource(getBlock().getRelative(BlockFace.DOWN));
        recalculateBrewTime();
        if (getBrewTime() % Config.config().cauldrons().cookingMinuteTicks() == 0) {
            this.recipe = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry())
                    .orElse(null);
        }
        Optional<Recipe<ItemStack>> recipeOptional = Optional.ofNullable(recipe);
        Color resultColor = computeResultColor(recipeOptional);
        Color baseParticleColor = computeBaseParticleColor(getBlock());
        this.particleColor = recipeOptional.map(recipe -> computeParticleColor(baseParticleColor, resultColor, recipe))
                .orElse(Color.GRAY);

        this.playBrewingEffects();
    }

    private long getBrewTime() {
        BrewingStep.TimedStep timedStep = (BrewingStep.TimedStep) brew.lastStep();
        return timedStep.time().moment();
    }

    private Color computeParticleColor(Color baseColor, Color resultColor, Recipe<ItemStack> recipe) {
        if (brew.lastStep() instanceof BrewingStep.Cook cook) {
            BrewingStep.Cook expectedCook = (BrewingStep.Cook) recipe.getSteps().get(brew.getCompletedSteps().size() - 1);
            return ColorUtil.getNextColor(baseColor, resultColor, cook.time().moment(), expectedCook.time().moment());
        } else if (brew.lastStep() instanceof BrewingStep.Mix mix) {
            BrewingStep.Mix expectedMix = (BrewingStep.Mix) recipe.getSteps().get(brew.getCompletedSteps().size() - 1);
            return ColorUtil.getNextColor(baseColor, resultColor, mix.time().moment(), expectedMix.time().moment());
        }
        return baseColor;
    }

    private Color computeResultColor(Optional<Recipe<ItemStack>> recipeOptional) {
        if (recipeOptional.isEmpty()) {
            return Color.GRAY;
        }
        Map<? extends Ingredient, Integer> ingredients;
        if (brew.lastStep() instanceof BrewingStep.Cook cook) {
            ingredients = cook.ingredients();
        } else if (brew.lastStep() instanceof BrewingStep.Mix mix) {
            ingredients = mix.ingredients();
        } else {
            return Color.AQUA;
        }
        BrewScore score = brew.score(recipeOptional.get());
        return !score.completed() ?
                IngredientUtil.ingredientData(ingredients).first() :
                ((BukkitRecipeResult) recipeOptional.get().getRecipeResults().getOrDefault(score.brewQuality(), BukkitRecipeResult.GENERIC)).getColor();
    }


    public void remove() {
        ListenerUtil.removeActiveSinglePositionStructure(this, TheBrewingProject.getInstance().getBreweryRegistry(), TheBrewingProject.getInstance().getDatabase());
    }

    public boolean addIngredient(@NotNull ItemStack item, Player player) {
        // TODO: Add API event
        if (!player.hasPermission("brewery.cauldron.access")) {
            MessageUtil.message(player, "tbp.cauldron.access-denied");
            return false;
        }
        if (!brewExtracted && item.getType() == Material.POTION) {
            BukkitCauldron.incrementLevel(getBlock());
        }
        this.hot = isHeatSource(getBlock().getRelative(BlockFace.DOWN));
        long time = TheBrewingProject.getInstance().getTime();
        Ingredient ingredient = BukkitIngredientManager.INSTANCE.getIngredient(item);
        if (hot) {
            brew = brew.withLastStep(BrewingStep.Cook.class,
                    cook -> {
                        Map<Ingredient, Integer> ingredients = new HashMap<>(cook.ingredients());
                        int amount = ingredients.computeIfAbsent(ingredient, ignored -> 0);
                        ingredients.put(ingredient, amount + 1);
                        return cook.withIngredients(ingredients);
                    },
                    () -> new CookStepImpl(new Interval(time, time), Map.of(BukkitIngredientManager.INSTANCE.getIngredient(item), 1), findCauldronType(getBlock()))
            );
        } else {
            brew = brew.withLastStep(BrewingStep.Mix.class,
                    mix -> {
                        Map<Ingredient, Integer> ingredients = new HashMap<>(mix.ingredients());
                        int amount = ingredients.computeIfAbsent(ingredient, ignored -> 0);
                        ingredients.put(ingredient, amount + 1);
                        return mix.withIngredients(ingredients);
                    },
                    () -> new MixStepImpl(new Interval(time, time), Map.of(BukkitIngredientManager.INSTANCE.getIngredient(item), 1))
            );
        }
        this.recipe = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry())
                .orElse(null);

        playIngredientAddedEffects(item);
        return true;
    }

    private Color computeBaseParticleColor(Block block) {
        return switch (block.getType()) {
            case WATER_CAULDRON -> WATER_COLOR;
            case LAVA_CAULDRON -> LAVA_COLOR;
            case POWDER_SNOW_CAULDRON -> SNOW_COLOR;
            default -> throw new IllegalStateException("Expected block to be cauldron type");
        };
    }

    public void playBrewingEffects() {
        Block block = getBlock();
        Location particleLoc = // Complex particle location based off BreweryX
                block.getLocation().add(0.5 + (RANDOM.nextDouble() * 0.8 - 0.4), 0.9, 0.5 + (RANDOM.nextDouble() * 0.8 - 0.4));

        block.getWorld().spawnParticle(Particle.ENTITY_EFFECT, particleLoc, 0, particleColor);


        if (!Config.config().cauldrons().minimalParticles() || !hot) {
            return;
        }

        if (RANDOM.nextFloat() > 0.85) {
            // Dark pixely smoke cloud at 0.4 random in x and z
            // 0 count enables direction, send to y = 1 with speed 0.09
            block.getWorld().spawnParticle(Particle.LARGE_SMOKE, particleLoc, 0, 0, 1, 0, 0.09);
        }
        if (RANDOM.nextFloat() > 0.2) {
            // A Water Splash with 0.2 offset in x and z
            block.getWorld().spawnParticle(Particle.SPLASH, particleLoc, 1, 0.2, 0, 0.2);
        }
        if (RANDOM.nextFloat() > 0.4) {
            // Two hovering pixely dust clouds, a bit of offset and with DustOptions to give some color and size
            block.getWorld().spawnParticle(Particle.DUST_PLUME, particleLoc, 2, 0.15, 0.2, 0.15, new Particle.DustOptions(particleColor, 1.5f));
        }
    }

    public void playIngredientAddedEffects(ItemStack item) {
        BukkitAdapter.toLocation(this.location)
                .map(Location::toCenterLocation)
                .filter(Location::isChunkLoaded)
                .ifPresent(bukkitLocation -> {
                    World world = bukkitLocation.getWorld();

                    SoundDefinition sound = item.getType() == Material.POTION ? Config.config().sounds().cauldronIngredientAddBrew() : Config.config().sounds().cauldronIngredientAdd();
                    SoundPlayer.playSoundEffect(sound, Sound.Source.BLOCK, bukkitLocation);

                    if (bukkitLocation.getBlock().getType() == Material.WATER_CAULDRON) {
                        world.spawnParticle(Particle.SPLASH, bukkitLocation.add(0.0, 0.5, 0.0), 50, 0.1, 0.05, 0.1, 1.0);
                    }
                });
    }

    public void playBrewExtractedEffects() {
        BukkitAdapter.toWorld(location).ifPresent(world ->
                SoundPlayer.playSoundEffect(
                        Config.config().sounds().cauldronBrewExtract(), Sound.Source.BLOCK,
                        world, location.x() + 0.5, location.y() + 1, location.z() + 0.5)
        );
    }

    public static boolean isHeatSource(Block block) {
        if (Config.config().cauldrons().heatSources().isEmpty()) {
            return true;
        }
        Material material = block.getType();
        if (!Config.config().cauldrons().heatSources().contains(BukkitAdapter.toMaterialHolder(material))) {
            return false;
        }
        if (material == Material.CAMPFIRE || material == Material.SOUL_CAMPFIRE) {
            return BlockUtil.isLitCampfire(block);
        } else if (material == Material.LAVA) {
            return BlockUtil.isSource(block);
        }
        return true;
    }

    @Override
    public BreweryLocation position() {
        return location;
    }

    public ItemStack extractBrew() {
        recalculateBrewTime();
        this.brewExtracted = true;
        playBrewExtractedEffects();
        return BrewAdapter.toItem(brew, new Brew.State.Other());
    }

    private void recalculateBrewTime() {
        long time = TheBrewingProject.getInstance().getTime();
        if (hot) {
            brew = brew.withLastStep(BrewingStep.Cook.class,
                    cook -> cook.withBrewTime(cook.time().withLastStep(time)),
                    () -> new CookStepImpl(new Interval(time, time), Map.of(), findCauldronType(getBlock())));
        } else {
            brew = brew.withLastStep(BrewingStep.Mix.class,
                    mix -> mix.withTime(mix.time().withLastStep(time)),
                    () -> new MixStepImpl(new Interval(time, time), Map.of())
            );
        }
    }

    private Block getBlock() {
        return BukkitAdapter.toBlock(location).orElseThrow(() -> new IllegalStateException("Could not find world for cauldron"));
    }

    public static void incrementLevel(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Levelled levelled) {
            levelled.setLevel(Math.min(levelled.getLevel() + 1, levelled.getMaximumLevel()));
            block.setBlockData(levelled);
        } else {
            Levelled waterCauldron = BlockType.WATER_CAULDRON.createBlockData();
            waterCauldron.setLevel(1);
            block.setBlockData(waterCauldron);
        }
    }

    public static boolean decrementLevel(Block block) {
        if (!Tag.CAULDRONS.isTagged(block.getType())) {
            return true;
        }
        if (!(block.getBlockData() instanceof Levelled levelled)) {
            block.setType(Material.CAULDRON);
            return true;
        }
        if (levelled.getLevel() == 1) {
            block.setType(Material.CAULDRON);
            return true;
        }
        levelled.setLevel(levelled.getLevel() - 1);
        block.setBlockData(levelled);
        return false;
    }

    public long getTime() {
        if (brew.getCompletedSteps().isEmpty()) {
            return 0L;
        }
        if (brew.lastStep() instanceof BrewingStep.Cook cook) {
            return cook.time().moment();
        }
        if (brew.lastStep() instanceof BrewingStep.Mix mix) {
            return mix.time().moment();
        }
        return 0L;
    }
}
