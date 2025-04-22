package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.listeners.ListenerUtil;
import dev.jsinco.brewery.bukkit.util.BlockUtil;
import dev.jsinco.brewery.bukkit.util.ColorUtil;
import dev.jsinco.brewery.bukkit.util.IngredientUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.util.Registry;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.vector.BreweryLocation;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class BukkitCauldron implements dev.jsinco.brewery.breweries.Cauldron {

    private static final Random RANDOM = new Random();

    private final Block block;
    @Getter
    private final CauldronType cauldronType;
    @Getter
    @Setter
    private boolean hot;
    @Getter
    private Brew brew;
    private Color particleColor = Color.AQUA;
    private boolean brewExtracted = false;


    public BukkitCauldron(Block block) {
        this(new HashMap<>(), block, TheBrewingProject.getInstance().getTime());
    }

    public BukkitCauldron(Map<Ingredient, Integer> ingredients, Block block, long brewStart) {
        this.block = block;
        this.cauldronType = findCauldronType(block);
        this.hot = isHeatSource(block.getRelative(BlockFace.DOWN));
        this.brew = hot ?
                new BrewImpl(new BrewingStep.Cook(new Interval(brewStart, brewStart), ingredients, cauldronType))
                : new BrewImpl(new BrewingStep.Mix(new Interval(brewStart, brewStart), ingredients));
    }

    public BukkitCauldron(Brew brew, Block block) {
        this.block = block;
        this.hot = isHeatSource(block.getRelative(BlockFace.DOWN));
        this.cauldronType = findCauldronType(block);
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
        if (!BlockUtil.isChunkLoaded(block)) {
            return;
        }
        this.hot = isHeatSource(block.getRelative(BlockFace.DOWN));
        Optional<Recipe<ItemStack>> recipeOptional = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry());
        if (recipeOptional.isPresent() && recipeOptional.get().getSteps().get(brew.getSteps().size() - 1) instanceof BrewingStep.Cook recipeCook) {
            recalculateBrewTime();
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
        if (brewExtracted) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.CAULDRON_CANT_ADD_MORE_INGREDIENTS));
            return false;
        }
        this.hot = isHeatSource(block.getRelative(BlockFace.DOWN));
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
                    () -> new BrewingStep.Cook(new Interval(time, time), Map.of(BukkitIngredientManager.INSTANCE.getIngredient(item), 1), cauldronType)
            );
        } else {
            brew = brew.withLastStep(BrewingStep.Mix.class,
                    mix -> {
                        Map<Ingredient, Integer> ingredients = new HashMap<>(mix.ingredients());
                        int amount = ingredients.computeIfAbsent(ingredient, ignored -> 0);
                        ingredients.put(ingredient, amount + 1);
                        return mix.withIngredients(ingredients);
                    },
                    () -> new BrewingStep.Mix(new Interval(time, time), Map.of(BukkitIngredientManager.INSTANCE.getIngredient(item), 1))
            );
        }
        return true;
    }

    public void playBrewingEffects() {
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
        return new BreweryLocation(block.getX(), block.getY(), block.getZ(), block.getWorld().getUID());
    }

    public ItemStack extractBrew() {
        recalculateBrewTime();
        this.brewExtracted = true;
        return BrewAdapter.toItem(brew, new Brew.State.Other());
    }

    private void recalculateBrewTime() {
        long time = TheBrewingProject.getInstance().getTime();
        if (hot) {
            brew = brew.withLastStep(BrewingStep.Cook.class,
                    cook -> cook.withBrewTime(cook.brewTime().withLastStep(time)),
                    () -> new BrewingStep.Cook(new Interval(time, time), Map.of(), this.cauldronType));
        } else {
            brew = brew.withLastStep(BrewingStep.Mix.class,
                    mix -> mix.withTime(mix.time().withLastStep(time)),
                    () -> new BrewingStep.Mix(new Interval(time, time), Map.of())
            );
        }
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
}
