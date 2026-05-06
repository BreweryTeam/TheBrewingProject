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
import dev.jsinco.brewery.api.recipe.DefaultRecipe;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.api.util.CancelState;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.brew.MixStepImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.animation.AnimationManager;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.bukkit.api.event.process.BrewCauldronProcessEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronInsertEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.brew.BrewAdapterAccess;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.listener.ListenerUtil;
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult;
import dev.jsinco.brewery.bukkit.util.BlockUtil;
import dev.jsinco.brewery.bukkit.util.BukkitIngredientUtil;
import dev.jsinco.brewery.bukkit.util.color.ColorUtil;
import dev.jsinco.brewery.bukkit.util.SoundPlayer;
import dev.jsinco.brewery.configuration.AnimationDisplay;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.ParticleDefinition;
import dev.jsinco.brewery.sound.SoundDefinition;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.Vibration;
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
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class BukkitCauldron implements Cauldron {

    private static final Random RANDOM = new Random();

    private final BreweryLocation location;
    private boolean hot = false;
    private Brew brew;
    private boolean brewExtracted = false;
    private Color particleColor = Color.fromRGB(0x3F76E4);
    private @Nullable Recipe<ItemStack> recipe;
    private @Nullable BrewQuality quality;
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
        BukkitAdapter.scheduleIfLoaded(location, TheBrewingProject.getInstance(), bukkitLocation -> {
            if (!Tag.CAULDRONS.isTagged(bukkitLocation.getBlock().getType()) || getBlock().getType() == Material.CAULDRON) {
                ListenerUtil.removeActiveSinglePositionStructure(this);
                return;
            }
            this.hot = isHeatSource(getBlock().getRelative(BlockFace.DOWN));
            recalculateBrewTime();
            if (Config.config().cauldrons().coloredWater() && (waterColorer == null || waterColorer.isDead())) {
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
                this.quality = Optional.ofNullable(recipe)
                        .flatMap(brew::quality)
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
        });
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
        int lastIdx = brew.getCompletedSteps().size() - 1;
        if (lastIdx >= recipe.getSteps().size()) {
            return baseColor;
        }
        if (brew.lastStep() instanceof BrewingStep.Cook cook) {
            BrewingStep.Cook expectedCook = (BrewingStep.Cook) recipe.getSteps().get(lastIdx);
            return ColorUtil.getNextColor(baseColor, resultColor, cook.time().moment(), expectedCook.time().moment());
        } else if (brew.lastStep() instanceof BrewingStep.Mix mix) {
            BrewingStep.Mix expectedMix = (BrewingStep.Mix) recipe.getSteps().get(lastIdx);
            return ColorUtil.getNextColor(baseColor, resultColor, mix.time().moment(), expectedMix.time().moment());
        }
        return baseColor;
    }

    private Color computeResultColor(Optional<Recipe<ItemStack>> recipeOptional) {
        if (recipeOptional.isEmpty()) {
            return convert(Config.config().cauldrons().failedParticleColor());
        }
        Optional<Color> defaultRecipeColor = BrewAdapterAccess.getDefaultRecipe(
                        recipeOptional,
                        TheBrewingProject.getInstance().getRecipeRegistry(),
                        brew,
                        false
                ).map(DefaultRecipe::result)
                .map(RecipeResult::newLorelessItem)
                .filter(itemStack -> itemStack.hasData(DataComponentTypes.POTION_CONTENTS))
                .map(itemStack -> itemStack.getData(DataComponentTypes.POTION_CONTENTS))
                .flatMap(potionContents -> Optional.ofNullable(potionContents.customColor()));
        if (defaultRecipeColor.isPresent()) {
            return defaultRecipeColor.get();
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

    public boolean addIngredient(@NonNull ItemStack item, Player player) {
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
        Optional<Brew> addedBrewOpt = BrewAdapterAccess.fromItem(addedItem);
        boolean isTBPBrew = addedBrewOpt.isPresent() || BrewAdapterAccess.isBrew(addedItem);
        if (isTBPBrew) {
            return handleBrewReaddition(addedItem, addedBrewOpt, player);
        }

        if (!brewExtracted && addedItem.getType() == Material.POTION) {
            BukkitCauldron.incrementLevel(getBlock());
            updateLevel(getBlock().getBlockData());
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
                    () -> new MixStepImpl(new Interval(time, time), Map.of(BukkitIngredientManager.INSTANCE.getIngredient(addedItem), 1), cauldronType)
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
        this.quality = Optional.ofNullable(recipe)
                .flatMap(brew::quality)
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

    private boolean handleBrewReaddition(ItemStack item, Optional<Brew> addedBrewOpt, Player player) {
        boolean isSealed = addedBrewOpt.isEmpty();

        if (!cauldronHasActiveBrew()) {
            // Empty cauldron: reinsert brew into the cauldron with 1 water level
            if (isSealed && !Config.config().cauldrons().allowSealedBrewContinuation()) {
                return false;
            }
            addedBrewOpt.ifPresent(value -> this.brew = value);
            BukkitCauldron.incrementLevel(getBlock());
            updateLevel(getBlock().getBlockData());
            this.brewExtracted = false;
            this.dirty = true;
            this.hot = isHeatSource(getBlock().getRelative(BlockFace.DOWN));
            recalculateBrewTime();
            this.recipe = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry()).orElse(null);
            this.quality = Optional.ofNullable(recipe).flatMap(brew::quality).orElse(null);
            playBrewInsertEffects(item, player);
            return true;
        }

        // Cauldron has an active brew: merge when it holds the same recipe
        if (!isSealed) {
            Brew addedBrew = addedBrewOpt.get();
            Optional<Recipe<ItemStack>> existingRecipeOpt = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry());
            Optional<Recipe<ItemStack>> addedRecipeOpt = addedBrew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry());
            boolean sameRecipe = existingRecipeOpt.isPresent() && addedRecipeOpt.isPresent()
                    && existingRecipeOpt.get().getRecipeName().equals(addedRecipeOpt.get().getRecipeName());
            if (sameRecipe) {
                this.brew = mergeBrews(this.brew, addedBrew);
                BukkitCauldron.incrementLevel(getBlock());
                updateLevel(getBlock().getBlockData());
                this.dirty = true;
                this.recipe = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry()).orElse(null);
                this.quality = Optional.ofNullable(recipe).flatMap(brew::quality).orElse(null);
                playBrewInsertEffects(item, player);
                return true;
            }
        }

        // Different recipe or sealed brew: add as an ingredient
        if (isSealed && !Config.config().cauldrons().allowSealedBrewAsIngredient()) {
            return false;
        }
        this.hot = isHeatSource(getBlock().getRelative(BlockFace.DOWN));
        CauldronType cauldronType = getType().orElseThrow(() -> new IllegalStateException("Expected cauldron block type for cauldron"));
        long time = TheBrewingProject.getInstance().getTime();
        Ingredient ingredient = BukkitIngredientManager.INSTANCE.getIngredient(item);
        Brew mixed;
        if (hot) {
            mixed = brew.withLastStep(BrewingStep.Cook.class,
                    cook -> {
                        Map<Ingredient, Integer> ingredients = new HashMap<>(cook.ingredients());
                        ingredients.merge(ingredient, 1, Integer::sum);
                        return cook.withIngredients(ingredients);
                    },
                    () -> new CookStepImpl(new Interval(time, time), Map.of(ingredient, 1), cauldronType)
            );
        } else {
            mixed = brew.withLastStep(BrewingStep.Mix.class,
                    mix -> {
                        Map<Ingredient, Integer> ingredients = new HashMap<>(mix.ingredients());
                        ingredients.merge(ingredient, 1, Integer::sum);
                        return mix.withIngredients(ingredients);
                    },
                    () -> new MixStepImpl(new Interval(time, time), Map.of(ingredient, 1), cauldronType)
            );
        }
        BrewCauldronProcessEvent mixEvent = new BrewCauldronProcessEvent(this, cauldronType, hot, brew, mixed);
        if (!mixEvent.callEvent()) {
            return true;
        }
        brew = mixEvent.getResult().witModifiedLastStep(step ->
                step instanceof BrewingStep.AuthoredStep<?> authoredStep
                        ? authoredStep.withBrewer(player.getUniqueId()) : step
        );
        this.recipe = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry()).orElse(null);
        this.quality = Optional.ofNullable(recipe).flatMap(brew::quality).orElse(null);
        playBrewInsertEffects(item, player);
        return true;
    }

    private void playBrewInsertEffects(ItemStack item, Player player) {
        long delay;
        if (Config.config().cauldrons().ingredientAddedAnimation() != AnimationDisplay.NONE) {
            delay = AnimationManager.playIngredientAddAnimation(item, player, getBlock().getLocation().toCenterLocation());
        } else {
            delay = 1;
        }
        playIngredientAddedEffects(item, delay);
    }

    private boolean cauldronHasActiveBrew() {
        return brew.getCompletedSteps().stream().anyMatch(step ->
                (step instanceof BrewingStep.IngredientsStep i && !i.ingredients().isEmpty())
                        || step instanceof BrewingStep.Distill
                        || step instanceof BrewingStep.Age
        );
    }

    private Brew mergeBrews(Brew existing, Brew added) {
        long currentTime = TheBrewingProject.getInstance().getTime();
        List<BrewingStep> existingSteps = existing.getCompletedSteps();
        List<BrewingStep> addedSteps = added.getCompletedSteps();
        int mergeCount = Math.min(existingSteps.size(), addedSteps.size());
        List<BrewingStep> mergedSteps = new ArrayList<>(existingSteps.size());
        for (int i = 0; i < mergeCount; i++) {
            mergedSteps.add(mergeStep(existingSteps.get(i), addedSteps.get(i), currentTime));
        }
        for (int i = mergeCount; i < existingSteps.size(); i++) {
            mergedSteps.add(existingSteps.get(i));
        }
        return existing.withStepsReplaced(mergedSteps);
    }

    private BrewingStep mergeStep(BrewingStep a, BrewingStep b, long currentTime) {
        if (a instanceof BrewingStep.Cook cookA && b instanceof BrewingStep.Cook cookB) {
            long avgElapsed = (cookA.time().moment() + cookB.time().moment()) / 2;
            return cookA.withBrewTime(new PassedMoment(avgElapsed).withLastStep(currentTime))
                        .withIngredients(averageIngredients(cookA.ingredients(), cookB.ingredients()));
        }
        if (a instanceof BrewingStep.Mix mixA && b instanceof BrewingStep.Mix mixB) {
            long avgElapsed = (mixA.time().moment() + mixB.time().moment()) / 2;
            return mixA.withTime(new PassedMoment(avgElapsed).withLastStep(currentTime))
                       .withIngredients(averageIngredients(mixA.ingredients(), mixB.ingredients()));
        }
        if (a instanceof BrewingStep.Distill distillA && b instanceof BrewingStep.Distill distillB) {
            return new DistillStepImpl((distillA.runs() + distillB.runs() + 1) / 2, distillA.brewers());
        }
        if (a instanceof BrewingStep.Age ageA && b instanceof BrewingStep.Age ageB) {
            long avgElapsed = (ageA.time().moment() + ageB.time().moment()) / 2;
            return ageA.withAge(new PassedMoment(avgElapsed));
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    private static Map<Ingredient, Integer> averageIngredients(Map<? extends Ingredient, Integer> a, Map<? extends Ingredient, Integer> b) {
        Map<Ingredient, Integer> mapA = (Map<Ingredient, Integer>) a;
        Map<Ingredient, Integer> mapB = (Map<Ingredient, Integer>) b;
        Set<Ingredient> allKeys = new HashSet<>(mapA.keySet());
        allKeys.addAll(mapB.keySet());
        Map<Ingredient, Integer> result = new HashMap<>();
        for (Ingredient key : allKeys) {
            int countA = mapA.getOrDefault(key, 0);
            int countB = mapB.getOrDefault(key, 0);
            int avg = (countA + countB + 1) / 2;
            if (avg > 0) result.put(key, avg);
        }
        return result;
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
        World world = block.getWorld();
        Supplier<Location> locationSupplier = () ->
                block.getLocation().add(0.5 + (RANDOM.nextDouble() * 0.8 - 0.4), 0.9, 0.5 + (RANDOM.nextDouble() * 0.8 - 0.4));
        double progress;
        if (brew.lastStep() instanceof BrewingStep.TimedStep timedStep) {
            if (recipe != null && brew.stepAmount() - 1 < recipe.getSteps().size()
                    && recipe.getSteps().get(brew.stepAmount() - 1) instanceof BrewingStep.TimedStep expectedTimed) {
                progress = (double) timedStep.time().moment() / expectedTimed.time().moment();
            } else {
                progress = Math.min((double) timedStep.time().moment() / Moment.MINUTE * 3, 1);
            }
        } else {
            progress = 1D; // Shouldn't happen
        }
        List<ParticleDefinition> definitions = hot ? Config.config().cauldrons().cookParticleDefinitions() : Config.config().cauldrons().mixParticleDefinitions();
        definitions.stream()
                .filter(particleDefinition -> particleDefinition.range() == null || particleDefinition.range().isWithin(progress))
                .filter(particleDefinition -> particleDefinition.quality() == null || particleDefinition.quality().equals(quality))
                .filter(particleDefinition -> particleDefinition.probability() > RANDOM.nextDouble())
                .map(ParticleDefinition::particleKey)
                .map(BukkitAdapter::toNamespacedKey)
                .filter(Objects::nonNull)
                .map(Registry.PARTICLE_TYPE::get)
                .filter(Objects::nonNull)
                .forEach(particle -> {
                    Class<?> dataType = particle.getDataType();
                    if (dataType == Void.class) {
                        world.spawnParticle(particle, locationSupplier.get(), 0);
                    } else if (dataType == Color.class) {
                        world.spawnParticle(particle, locationSupplier.get(), 0, particleColor);
                    } else if (dataType == Particle.DustOptions.class) {
                        world.spawnParticle(particle, locationSupplier.get(), 0, new Particle.DustOptions(particleColor, 1.6F));
                    } else if (dataType == BlockData.class) {
                        world.spawnParticle(particle, locationSupplier.get(), 0, getBlock().getBlockData());
                    } else if (dataType == Float.class) {
                        world.spawnParticle(particle, locationSupplier.get(), 0, 1F);
                    } else if (dataType == Particle.Spell.class) {
                        world.spawnParticle(particle, locationSupplier.get(), 0, new Particle.Spell(particleColor, 1F));
                    } else if (dataType == ItemStack.class) {
                        // TODO: Take items from input ingredients (would require conversion)
                    } else if (dataType == Integer.class) {
                        world.spawnParticle(particle, locationSupplier.get(), 0, 1);
                    } else if (dataType == Particle.Trail.class) {
                        world.spawnParticle(particle, locationSupplier.get(), 0, new Particle.Trail(locationSupplier.get(), particleColor, 10));
                    } else if (dataType == Vibration.class) {
                        world.spawnParticle(particle, locationSupplier.get(), 0, new Vibration(new Vibration.Destination.BlockDestination(getBlock()), 10));
                    }
                });
    }

    public void playIngredientAddedEffects(ItemStack item, long delay) {
        BukkitAdapter.toLocation(this.location)
                .map(Location::toCenterLocation)
                .filter(Location::isChunkLoaded)
                .ifPresent(bukkitLocation -> Bukkit.getRegionScheduler().runDelayed(TheBrewingProject.getInstance(), bukkitLocation, ignored -> {
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
                    () -> new MixStepImpl(new Interval(time, time), Map.of(),
                            getType().orElseThrow(() -> new IllegalStateException("Expected cauldron block type for cauldron")))
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
        updateLevel(levelled);
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

    @NonNull
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

    @Override
    public CompletableFuture<Void> runLocally(Runnable action) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        Bukkit.getRegionScheduler().run(TheBrewingProject.getInstance(), BukkitAdapter.toLocation(location).orElseThrow(), ignored -> {
            action.run();
            completableFuture.complete(null);
        });
        return completableFuture;
    }
}
