package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.brew.*;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.listeners.ListenerUtil;
import dev.jsinco.brewery.bukkit.util.BlockUtil;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.ColorUtil;
import dev.jsinco.brewery.bukkit.util.IngredientUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.util.Registry;
import dev.jsinco.brewery.vector.BreweryLocation;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BukkitCauldron implements dev.jsinco.brewery.breweries.Cauldron {

    private static final Random RANDOM = new Random();

    private final BreweryLocation location;
    @Getter
    @Setter
    private boolean hot = false;
    @Getter
    private Brew brew;
    private Color particleColor = Color.AQUA;
    private boolean brewExtracted = false;


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
        this.hot = isHeatSource(getBlock().getRelative(BlockFace.DOWN));
        Optional<Recipe<ItemStack>> recipeOptional = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry());
        recalculateBrewTime();
        if (recipeOptional.isPresent() && recipeOptional.get().getSteps().get(brew.getCompletedSteps().size() - 1) instanceof BrewingStep.Cook recipeCook) {
            if (hot) {
                BrewingStep.Cook cook = (BrewingStep.Cook) brew.lastStep();
                this.particleColor = ColorUtil.getNextColor(Color.AQUA, IngredientUtil.ingredientData(cook.ingredients()).first(), cook.brewTime().moment(), recipeCook.brewTime().moment());
            } else {
                this.particleColor = Color.AQUA;
            }
        }
        this.playBrewingEffects();
    }


    public void remove() {
        ListenerUtil.removeActiveSinglePositionStructure(this, TheBrewingProject.getInstance().getBreweryRegistry(), TheBrewingProject.getInstance().getDatabase());
    }

    public boolean addIngredient(@NotNull ItemStack item, Player player) {
        // TODO: Add API event
        if (!player.hasPermission("brewery.cauldron.access")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.CAULDRON_ACCESS_DENIED));
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

        playIngredientAddedEffects(item);
        return true;
    }

    public void playBrewingEffects() {
        Block block = getBlock();
        Location particleLoc = // Complex particle location based off BreweryX
                block.getLocation().add(0.5 + (RANDOM.nextDouble() * 0.8 - 0.4), 0.9, 0.5 + (RANDOM.nextDouble() * 0.8 - 0.4));

        block.getWorld().spawnParticle(Particle.ENTITY_EFFECT, particleLoc, 0, particleColor);


        if (!Config.MINIMAL_PARTICLES || !hot) {
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
        Location bukkitLocation = BukkitAdapter.toLocation(this.location).toCenterLocation();
        World world = bukkitLocation.getWorld();

        Sound sound = item.getType() == Material.POTION
                ? Sound.sound().source(Sound.Source.BLOCK).type(Key.key("minecraft:block.pointed_dripstone.drip_water_into_cauldron")).pitch(0.85f).build()
                : Sound.sound().source(Sound.Source.BLOCK).type(Key.key("minecraft:entity.generic.splash")).pitch(1.5f + RANDOM.nextFloat(0.2f) - 0.1f).build();
        world.playSound(
                sound,
                bukkitLocation.x(), bukkitLocation.y(), bukkitLocation.z()
        );

        world.spawnParticle(Particle.SPLASH, bukkitLocation.add(0.0, 0.5, 0.0), 50, 0.1, 0.05, 0.1, 1.0);
    }

    public void playBrewExtractedEffects() {
        BukkitAdapter.toWorld(location).ifPresent(world -> world.playSound(
                Sound.sound().source(Sound.Source.BLOCK).type(Key.key("minecraft:item.bottle.fill")).build(),
                location.x() + 0.5, location.y() + 1, location.z() + 0.5
        ));
    }

    public static boolean isHeatSource(Block block) {
        if (Config.HEAT_SOURCES.isEmpty()) {
            return true;
        }
        Material material = block.getType();
        if (!Config.HEAT_SOURCES.contains(material.name().toLowerCase())) {
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
        playBrewingEffects();
        return BrewAdapter.toItem(brew, new Brew.State.Other());
    }

    private void recalculateBrewTime() {
        long time = TheBrewingProject.getInstance().getTime();
        if (hot) {
            brew = brew.withLastStep(BrewingStep.Cook.class,
                    cook -> cook.withBrewTime(cook.brewTime().withLastStep(time)),
                    () -> new CookStepImpl(new Interval(time, time), Map.of(), findCauldronType(getBlock())));
        } else {
            brew = brew.withLastStep(BrewingStep.Mix.class,
                    mix -> mix.withTime(mix.time().withLastStep(time)),
                    () -> new MixStepImpl(new Interval(time, time), Map.of())
            );
        }
    }

    private Block getBlock() {
        return BukkitAdapter.toBlock(location);
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
        if (!(block.getBlockData() instanceof Levelled levelled)) {
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
            return cook.brewTime().moment();
        }
        if (brew.lastStep() instanceof BrewingStep.Mix mix) {
            return mix.time().moment();
        }
        return 0L;
    }
}
