package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.moment.Interval;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.api.util.CancelState;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.MixStepImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.animation.AnimationManager;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.bukkit.api.event.process.BrewCauldronProcessEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronInsertEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.listener.ListenerUtil;
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult;
import dev.jsinco.brewery.bukkit.util.BlockUtil;
import dev.jsinco.brewery.bukkit.util.BukkitIngredientUtil;
import dev.jsinco.brewery.bukkit.util.ColorUtil;
import dev.jsinco.brewery.bukkit.util.SoundPlayer;
import dev.jsinco.brewery.configuration.AnimationDisplay;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.sound.SoundDefinition;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class BukkitCauldron implements Cauldron {

    private static final Random RANDOM = new Random();

    private final BreweryLocation location;
    private boolean hot = false;
    private Brew brew;
    private boolean brewExtracted = false;
    private Color particleColor = Color.fromRGB(0x3F76E4);
    private @Nullable Recipe<ItemStack> recipe;
    private boolean dirty = true;
    private TextDisplay waterColorer = null;

    public BukkitCauldron(BreweryLocation location, boolean hot) {
        this.location = location;
        this.hot = hot;
        this.brew = new BrewImpl(List.of());
    }

    public BukkitCauldron(Brew brew, BreweryLocation location) {
        this.location = location;
        this.brew = brew;
    }

    private static Optional<CauldronType> findCauldronType(Block block) {
        return BreweryRegistry.CAULDRON_TYPE.values()
                .stream()
                .filter(cauldronType -> block.getType().getKey().toString().equals(cauldronType.materialKey()))
                .findAny();
    }

    @Override
    public void tick() {
        if (!BlockUtil.isChunkLoaded(location)) {
            return;
        }
        if (!Tag.CAULDRONS.isTagged(getBlock().getType()) || getBlock().getType() == Material.CAULDRON) {
            ListenerUtil.removeActiveSinglePositionStructure(this);
            return;
        }
        this.hot = isHeatSource(getBlock().getRelative(BlockFace.DOWN));
        recalculateBrewTime();
        Location bukkitLocation = BukkitAdapter.toLocation(location).orElse(null);
        if (Config.config().cauldrons().coloredWater() && bukkitLocation != null && (waterColorer == null || waterColorer.isDead())) {
            waterColorer = getBlock().getWorld().spawn(bukkitLocation.clone().add(0.5, 0, 0.5), TextDisplay.class, textDisplay -> {
                setWaterText(textDisplay);
                textDisplay.setTransformation(compileTransformation(bukkitLocation.getBlock().getBlockData()));
                textDisplay.setPersistent(false);
                textDisplay.setBackgroundColor(Color.fromARGB(0, 255, 255, 255));
            });
        }
        if (dirty || getBrewTime() % Config.config().cauldrons().cookingMinuteTicks() == 0) {
            this.recipe = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry())
                    .orElse(null);
            dirty = false;
        }
        Optional<Recipe<ItemStack>> recipeOptional = Optional.ofNullable(recipe);
        Color resultColor = computeResultColor(recipeOptional);
        Color baseParticleColor = computeBaseParticleColor(getBlock());
        this.particleColor = recipeOptional.map(recipe -> computeParticleColor(baseParticleColor, resultColor, recipe))
                .orElseGet(() -> ColorUtil.getNextColor(baseParticleColor, convert(Config.config().cauldrons().failedParticleColor()), getBrewTime(), Moment.MINUTE * 3));
        if (waterColorer != null) {
            setWaterText(waterColorer);
        }
        this.playBrewingEffects();
    }

    private void setWaterText(TextDisplay textDisplay) {
        Color newColor = ColorUtil.closestColorLimitedOpacity(particleColor, computeBaseParticleColor(getBlock()), Config.config().cauldrons().waterColorOpacity() & 0xFF);
        textDisplay.text(Component.text("█").color(TextColor.color(newColor.asRGB())));
        textDisplay.setTextOpacity((byte) (newColor.getAlpha() & 0xFF));
    }

    private Transformation compileTransformation(BlockData blockData) {
        float levelOffset;
        if (blockData instanceof Levelled levelled) {
            levelOffset = 6F / 16 + 9F / 16 * levelled.getLevel() / levelled.getMaximumLevel();
        } else {
            levelOffset = 15F / 16;
        }
        return new Transformation(
                new Vector3f(-1F / 16, levelOffset, 8F / 16),
                new AxisAngle4f(),
                new Vector3f(4F, 0, 4F),
                new AxisAngle4f((float) -Math.PI / 2, 1F, 0, 0)
        );
    }

    private long getBrewTime() {
        if (brew.lastStep() instanceof BrewingStep.TimedStep timedStep) {
            return timedStep.time().moment();
        }
        return 0L;
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
            return convert(Config.config().cauldrons().failedParticleColor());
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
        BrewQuality brewQuality = score.brewQuality();
        if (brewQuality == null) {
            return convert(Config.config().cauldrons().failedParticleColor());
        }
        return !score.completed() ?
                BukkitIngredientUtil.ingredientData(ingredients).first() :
                ((BukkitRecipeResult) recipeOptional.get().getRecipeResults().get(brewQuality)).getColor();
    }

    public boolean addIngredient(@NotNull ItemStack item, Player player) {
        CauldronInsertEvent insertEvent = new CauldronInsertEvent(this,
                new ItemSource.ItemBasedSource(item),
                player.hasPermission("brewery.cauldron.access") ?
                        new CancelState.Allowed() : new CancelState.PermissionDenied(Component.translatable("tbp.cauldron.access-denied")),
                player
        );
        if (!insertEvent.callEvent()) {
            if (insertEvent.getCancelState() instanceof CancelState.PermissionDenied(Component denyMessage)) {
                player.sendMessage(denyMessage);
            }
            return false;
        }
        ItemStack addedItem = insertEvent.getItemSource().get();
        if (!brewExtracted && addedItem.getType() == Material.POTION) {
            BukkitCauldron.incrementLevel(getBlock());
        }
        this.hot = isHeatSource(getBlock().getRelative(BlockFace.DOWN));
        CauldronType cauldronType = getType().orElseThrow(() -> new IllegalStateException("Expected cauldron block type for cauldron"));
        long time = TheBrewingProject.getInstance().getTime();
        Ingredient ingredient = BukkitIngredientManager.INSTANCE.getIngredient(addedItem);
        Brew mixed;
        if (hot) {
            mixed = brew.withLastStep(BrewingStep.Cook.class,
                    cook -> {
                        Map<Ingredient, Integer> ingredients = new HashMap<>(cook.ingredients());
                        int amount = ingredients.computeIfAbsent(ingredient, ignored -> 0);
                        ingredients.put(ingredient, amount + 1);
                        return cook.withIngredients(ingredients);
                    },
                    () -> new CookStepImpl(new Interval(time, time), Map.of(BukkitIngredientManager.INSTANCE.getIngredient(addedItem), 1), cauldronType)
            );
        } else {
            mixed = brew.withLastStep(BrewingStep.Mix.class,
                    mix -> {
                        Map<Ingredient, Integer> ingredients = new HashMap<>(mix.ingredients());
                        int amount = ingredients.computeIfAbsent(ingredient, ignored -> 0);
                        ingredients.put(ingredient, amount + 1);
                        return mix.withIngredients(ingredients);
                    },
                    () -> new MixStepImpl(new Interval(time, time), Map.of(BukkitIngredientManager.INSTANCE.getIngredient(addedItem), 1))
            );
        }
        BrewCauldronProcessEvent mixEvent = new BrewCauldronProcessEvent(this, cauldronType, hot, brew, mixed);
        if (!mixEvent.callEvent()) {
            return true;
        }
        brew = mixEvent.getResult()
                .witModifiedLastStep(step ->
                        step instanceof BrewingStep.AuthoredStep<?> authoredStep
                                ? authoredStep.withBrewer(player.getUniqueId()) : step
                );
        this.recipe = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry())
                .orElse(null);
        long delay;
        if (Config.config().cauldrons().ingredientAddedAnimation() != AnimationDisplay.NONE) {
            delay = AnimationManager.playIngredientAddAnimation(addedItem, player, getBlock().getLocation().toCenterLocation());
        } else {
            delay = 1;
        }
        playIngredientAddedEffects(addedItem, delay);
        return true;
    }

    private Color computeBaseParticleColor(Block block) {
        return switch (block.getType()) {
            case WATER_CAULDRON -> convert(Config.config().cauldrons().waterBaseParticleColor());
            case LAVA_CAULDRON -> convert(Config.config().cauldrons().lavaBaseParticleColor());
            case POWDER_SNOW_CAULDRON -> convert(Config.config().cauldrons().snowBaseParticleColor());
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

    public void playIngredientAddedEffects(ItemStack item, long delay) {
        BukkitAdapter.toLocation(this.location)
                .map(Location::toCenterLocation)
                .filter(Location::isChunkLoaded)
                .ifPresent(bukkitLocation -> Bukkit.getGlobalRegionScheduler().runDelayed(TheBrewingProject.getInstance(), ignored -> {
                    World world = bukkitLocation.getWorld();

                    SoundDefinition sound = item.getType() == Material.POTION ? Config.config().sounds().cauldronIngredientAddBrew() : Config.config().sounds().cauldronIngredientAdd();
                    SoundPlayer.playSoundEffect(sound, Sound.Source.BLOCK, bukkitLocation);

                    if (bukkitLocation.getBlock().getType() == Material.WATER_CAULDRON) {
                        world.spawnParticle(Particle.SPLASH, bukkitLocation.add(0.0, 0.5, 0.0), 50, 0.1, 0.05, 0.1, 1.0);
                    }
                }, delay));
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

    public Brew getUpdatedBrew() {
        recalculateBrewTime();
        return brew;
    }

    public void extractBrew() {
        this.brewExtracted = true;
        playBrewExtractedEffects();
    }

    private void recalculateBrewTime() {
        long time = TheBrewingProject.getInstance().getTime();
        if (hot) {
            brew = brew.withLastStep(BrewingStep.Cook.class,
                    cook -> cook.withBrewTime(cook.time().withLastStep(time)),
                    () -> new CookStepImpl(
                            new Interval(time, time),
                            Map.of(),
                            getType()
                                    .orElseThrow(() -> new IllegalStateException("Expected cauldron block type for cauldron"))
                    )
            );
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

    public boolean decrementLevel() {
        Block block = getBlock();
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

    public void updateLevel(BlockData newLevelData) {
        if (waterColorer != null) {
            waterColorer.setTransformation(compileTransformation(newLevelData));
        }
    }

    @Override
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

    @NotNull
    @Override
    public Optional<CauldronType> getType() {
        return findCauldronType(getBlock());
    }

    @Override
    public int getLevel() {
        if (getBlock().getType() == Material.LAVA_CAULDRON) {
            return 1;
        }
        if (getBlock().getBlockData() instanceof Levelled levelled) {
            return levelled.getLevel();
        }
        return 0;
    }

    @Override
    public void destroy() {
        if (waterColorer != null) {
            waterColorer.remove();
        }
    }

    private Color convert(java.awt.Color awtColor) {
        return Color.fromRGB(awtColor.getRGB() & 0xFFFFFF);
    }

    public boolean isHot() {
        return this.hot;
    }

    public Brew getBrew() {
        return this.brew;
    }

    public void setHot(boolean hot) {
        this.hot = hot;
    }
}
