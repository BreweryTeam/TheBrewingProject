package dev.jsinco.brewery.bukkit.brews;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.ingredient.SimpleIngredient;
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult;
import dev.jsinco.brewery.brew.BrewQuality;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.recipes.RecipeImpl;
import dev.jsinco.brewery.recipes.RecipeRegistryImpl;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.moment.PassedMoment;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockBukkitExtension.class)
class BrewTest {
    RecipeRegistryImpl<ItemStack> registry;

    @BeforeEach
    void setUp() {
        MockBukkit.load(TheBrewingProject.class);
        registry = TheBrewingProject.getInstance().getRecipeRegistry();
    }

    @Test
    void closestRecipe_differingIngredients() {
        setupRecipes();
        BrewImpl brew = new BrewImpl(
                List.of(
                        new BrewingStep.Cook(
                                new Interval(0, Interval.MINUTE * 10),
                                Map.of(
                                        new SimpleIngredient(Material.WHEAT), 11,
                                        new SimpleIngredient(Material.APPLE), 20
                                ),
                                CauldronType.WATER
                        ),
                        new BrewingStep.Distill(129),
                        new BrewingStep.Age(
                                new Interval(0, Interval.AGING_YEAR * 13),
                                BarrelType.ACACIA
                        )
                )
        );
        Recipe<ItemStack> closest = brew.closestRecipe(registry).get();
        assertEquals(registry.getRecipe("recipe2").get(), closest);
        assertTrue(brew.score(closest).completed());
        assertEquals(BrewQuality.EXCELLENT, brew.quality(closest).get());
    }

    @Test
    void closestRecipe_differingBarrelType() {
        setupRecipes();
        BrewImpl brew = new BrewImpl(
                List.of(
                        new BrewingStep.Cook(
                                new Interval(0, Interval.MINUTE * 10),
                                Map.of(
                                        new SimpleIngredient(Material.WHEAT), 10,
                                        new SimpleIngredient(Material.APPLE), 20
                                ),
                                CauldronType.WATER
                        ),
                        new BrewingStep.Distill(129),
                        new BrewingStep.Age(
                                new Interval(0, Interval.AGING_YEAR * 13),
                                BarrelType.BAMBOO
                        )
                )
        );
        Recipe<ItemStack> closest = brew.closestRecipe(registry).get();
        assertEquals(registry.getRecipe("recipe3").get(), closest);
        assertTrue(brew.score(closest).completed());
        assertEquals(BrewQuality.EXCELLENT, brew.quality(closest).get());
    }

    @Test
    void closestRecipe_differingCauldronType() {
        setupRecipes();
        BrewImpl brew = new BrewImpl(
                List.of(
                        new BrewingStep.Cook(
                                new Interval(0, Interval.MINUTE * 10),
                                Map.of(
                                        new SimpleIngredient(Material.WHEAT), 10,
                                        new SimpleIngredient(Material.APPLE), 20
                                ),
                                CauldronType.LAVA
                        ),
                        new BrewingStep.Distill(129),
                        new BrewingStep.Age(
                                new Interval(0, Interval.AGING_YEAR * 13),
                                BarrelType.ACACIA
                        )
                )
        );
        Recipe<ItemStack> closest = brew.closestRecipe(registry).get();
        assertEquals(registry.getRecipe("recipe4").get(), closest);
        assertTrue(brew.score(closest).completed());
        assertEquals(BrewQuality.EXCELLENT, brew.quality(closest).get());
    }

    @Test
    void closestRecipe_differingDistillRuns() {
        setupRecipes();
        BrewImpl brew = new BrewImpl(
                List.of(
                        new BrewingStep.Cook(
                                new Interval(0, Interval.MINUTE * 10),
                                Map.of(
                                        new SimpleIngredient(Material.WHEAT), 10,
                                        new SimpleIngredient(Material.APPLE), 20
                                ),
                                CauldronType.WATER
                        ),
                        new BrewingStep.Distill(128),
                        new BrewingStep.Age(
                                new Interval(0, Interval.AGING_YEAR * 13),
                                BarrelType.ACACIA
                        )
                )
        );
        Recipe<ItemStack> closest = brew.closestRecipe(registry).get();
        assertEquals(registry.getRecipe("recipe5").get(), closest);
        assertTrue(brew.score(closest).completed());
        assertEquals(BrewQuality.EXCELLENT, brew.quality(closest).get());
    }

    @Test
    void closestRecipe_differingBrewTime() {
        setupRecipes();
        BrewImpl brew = new BrewImpl(
                List.of(
                        new BrewingStep.Cook(
                                new Interval(0, Interval.MINUTE * 11),
                                Map.of(
                                        new SimpleIngredient(Material.WHEAT), 10,
                                        new SimpleIngredient(Material.APPLE), 20
                                ),
                                CauldronType.WATER
                        ),
                        new BrewingStep.Distill(129),
                        new BrewingStep.Age(
                                new Interval(0, Interval.AGING_YEAR * 13),
                                BarrelType.ACACIA
                        )
                )
        );
        Recipe<ItemStack> closest = brew.closestRecipe(registry).get();
        assertEquals(registry.getRecipe("recipe6").get(), closest);
        assertTrue(brew.score(closest).completed());
        assertEquals(BrewQuality.EXCELLENT, brew.quality(closest).get());
    }

    @Test
    void closestRecipe_differingAgingTime() {
        setupRecipes();
        BrewImpl brew = new BrewImpl(
                List.of(
                        new BrewingStep.Cook(
                                new Interval(0, Interval.MINUTE * 10),
                                Map.of(
                                        new SimpleIngredient(Material.WHEAT), 10,
                                        new SimpleIngredient(Material.APPLE), 20
                                ),
                                CauldronType.WATER
                        ),
                        new BrewingStep.Distill(129),
                        new BrewingStep.Age(
                                new Interval(0, Interval.AGING_YEAR * 10),
                                BarrelType.ACACIA
                        )
                )
        );
        Recipe<ItemStack> closest = brew.closestRecipe(registry).get();
        assertEquals(registry.getRecipe("recipe7").get(), closest);
        assertTrue(brew.score(closest).completed());
        assertEquals(BrewQuality.EXCELLENT, brew.quality(closest).get());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void quality_differingDifficulties(int difficulty) {
        RecipeImpl<ItemStack> recipe = new RecipeImpl.Builder<ItemStack>("test")
                .brewDifficulty(difficulty)
                .recipeResult(BukkitRecipeResult.GENERIC)
                .steps(
                        List.of(
                                new BrewingStep.Cook(
                                        new PassedMoment(8 * Moment.MINUTE),
                                        Map.of(new SimpleIngredient(Material.WHEAT), 3),
                                        CauldronType.WATER
                                ),
                                new BrewingStep.Age(
                                        new PassedMoment(2 * Moment.AGING_YEAR),
                                        BarrelType.BAMBOO
                                )
                        )
                )
                .build();
        Brew brew = new BrewImpl(
                List.of(
                        new BrewingStep.Cook(
                                new PassedMoment(8 * Moment.MINUTE),
                                Map.of(new SimpleIngredient(Material.WHEAT), 3),
                                CauldronType.WATER
                        ),
                        new BrewingStep.Age(
                                new PassedMoment(2 * Moment.AGING_YEAR),
                                BarrelType.BAMBOO
                        )
                )
        );
        assertEquals(BrewQuality.EXCELLENT, brew.quality(recipe).get());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void quality_increasingIngredient(int difficulty) {
        Recipe<ItemStack> recipe = new RecipeImpl.Builder<ItemStack>("test")
                .brewDifficulty(difficulty)
                .recipeResult(BukkitRecipeResult.GENERIC)
                .steps(
                        List.of(
                                new BrewingStep.Cook(
                                        new PassedMoment(8 * Moment.MINUTE),
                                        Map.of(new SimpleIngredient(Material.WHEAT), 3),
                                        CauldronType.WATER
                                ),
                                new BrewingStep.Age(
                                        new PassedMoment(2 * Moment.AGING_YEAR),
                                        BarrelType.BAMBOO
                                )
                        )
                )
                .build();
        boolean hasHadNullQuality = false;
        for (int i = 3; i < 10; i++) {
            BrewImpl brew = new BrewImpl(
                    List.of(
                            new BrewingStep.Cook(
                                    new PassedMoment(8 * Moment.MINUTE),
                                    Map.of(new SimpleIngredient(Material.WHEAT), i),
                                    CauldronType.WATER
                            ),
                            new BrewingStep.Age(
                                    new PassedMoment(2 * Moment.AGING_YEAR),
                                    BarrelType.BAMBOO
                            )
                    )
            );
            BrewQuality brewQuality = brew.quality(recipe).orElse(null);
            if (brewQuality == null || !brew.score(recipe).completed()) {
                hasHadNullQuality = true;
            }
        }
        assertTrue(hasHadNullQuality);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void quality_decreasingIngredient(int difficulty) {
        Recipe<ItemStack> recipe = new RecipeImpl.Builder<ItemStack>("test")
                .brewDifficulty(difficulty)
                .recipeResult(BukkitRecipeResult.GENERIC)
                .steps(
                        List.of(
                                new BrewingStep.Cook(
                                        new PassedMoment(8 * Moment.MINUTE),
                                        Map.of(new SimpleIngredient(Material.WHEAT), 3),
                                        CauldronType.WATER
                                ),
                                new BrewingStep.Age(
                                        new PassedMoment(2 * Moment.AGING_YEAR),
                                        BarrelType.BAMBOO
                                )
                        )
                )
                .build();
        boolean hasHadNullQuality = false;
        for (int i = 3; i >= 0; i--) {
            BrewImpl brew = new BrewImpl(
                    List.of(
                            new BrewingStep.Cook(
                                    new PassedMoment(8 * Moment.MINUTE),
                                    Map.of(new SimpleIngredient(Material.WHEAT), i),
                                    CauldronType.WATER
                            ),
                            new BrewingStep.Age(
                                    new PassedMoment(2 * Moment.AGING_YEAR),
                                    BarrelType.BAMBOO
                            )
                    )
            );
            BrewQuality brewQuality = brew.quality(recipe).orElse(null);
            if (brewQuality == null) {
                hasHadNullQuality = true;
            }
        }
        assertTrue(hasHadNullQuality);
    }

    void setupRecipes() {
        Recipe<ItemStack> recipe1 = new RecipeImpl.Builder<ItemStack>("recipe1")
                .brewDifficulty(10)
                .recipeResult(BukkitRecipeResult.GENERIC)
                .steps(
                        List.of(
                                new BrewingStep.Cook(
                                        new PassedMoment(10 * Moment.MINUTE),
                                        Map.of(
                                                new SimpleIngredient(Material.WHEAT), 10,
                                                new SimpleIngredient(Material.APPLE), 20
                                        ),
                                        CauldronType.WATER
                                ),
                                new BrewingStep.Distill(129),
                                new BrewingStep.Age(
                                        new PassedMoment(13 * Moment.AGING_YEAR),
                                        BarrelType.ACACIA
                                )
                        )
                )
                .build();
        RecipeImpl<ItemStack> recipe2 = new RecipeImpl.Builder<ItemStack>("recipe2")
                .brewDifficulty(10)
                .recipeResult(BukkitRecipeResult.GENERIC)
                .steps(
                        List.of(
                                new BrewingStep.Cook(
                                        new PassedMoment(10 * Moment.MINUTE),
                                        Map.of(
                                                new SimpleIngredient(Material.WHEAT), 11,
                                                new SimpleIngredient(Material.APPLE), 20
                                        ),
                                        CauldronType.WATER
                                ),
                                new BrewingStep.Distill(129),
                                new BrewingStep.Age(
                                        new PassedMoment(13 * Moment.AGING_YEAR),
                                        BarrelType.ACACIA
                                )
                        )
                )
                .build();
        RecipeImpl<ItemStack> recipe3 = new RecipeImpl.Builder<ItemStack>("recipe3")
                .brewDifficulty(10)
                .recipeResult(BukkitRecipeResult.GENERIC)
                .steps(
                        List.of(
                                new BrewingStep.Cook(
                                        new PassedMoment(10 * Moment.MINUTE),
                                        Map.of(
                                                new SimpleIngredient(Material.WHEAT), 10,
                                                new SimpleIngredient(Material.APPLE), 20
                                        ),
                                        CauldronType.WATER
                                ),
                                new BrewingStep.Distill(129),
                                new BrewingStep.Age(
                                        new PassedMoment(13 * Moment.AGING_YEAR),
                                        BarrelType.BAMBOO
                                )
                        )
                )
                .build();
        RecipeImpl<ItemStack> recipe4 = new RecipeImpl.Builder<ItemStack>("recipe4")
                .brewDifficulty(10)
                .recipeResult(BukkitRecipeResult.GENERIC)
                .steps(
                        List.of(
                                new BrewingStep.Cook(
                                        new PassedMoment(10 * Moment.MINUTE),
                                        Map.of(
                                                new SimpleIngredient(Material.WHEAT), 10,
                                                new SimpleIngredient(Material.APPLE), 20
                                        ),
                                        CauldronType.LAVA
                                ),
                                new BrewingStep.Distill(129),
                                new BrewingStep.Age(
                                        new PassedMoment(13 * Moment.AGING_YEAR),
                                        BarrelType.ACACIA
                                )
                        )
                )
                .build();
        RecipeImpl<ItemStack> recipe5 = new RecipeImpl.Builder<ItemStack>("recipe5")
                .brewDifficulty(10)
                .recipeResult(BukkitRecipeResult.GENERIC)
                .steps(
                        List.of(
                                new BrewingStep.Cook(
                                        new PassedMoment(10 * Moment.MINUTE),
                                        Map.of(
                                                new SimpleIngredient(Material.WHEAT), 10,
                                                new SimpleIngredient(Material.APPLE), 20
                                        ),
                                        CauldronType.WATER
                                ),
                                new BrewingStep.Distill(128),
                                new BrewingStep.Age(
                                        new PassedMoment(13 * Moment.AGING_YEAR),
                                        BarrelType.ACACIA
                                )
                        )
                )
                .build();
        RecipeImpl<ItemStack> recipe6 = new RecipeImpl.Builder<ItemStack>("recipe6")
                .brewDifficulty(10)
                .recipeResult(BukkitRecipeResult.GENERIC)
                .steps(
                        List.of(
                                new BrewingStep.Cook(
                                        new PassedMoment(11 * Moment.MINUTE),
                                        Map.of(
                                                new SimpleIngredient(Material.WHEAT), 10,
                                                new SimpleIngredient(Material.APPLE), 20
                                        ),
                                        CauldronType.WATER
                                ),
                                new BrewingStep.Distill(129),
                                new BrewingStep.Age(
                                        new PassedMoment(13 * Moment.AGING_YEAR),
                                        BarrelType.ACACIA
                                )
                        )
                )
                .build();
        RecipeImpl<ItemStack> recipe7 = new RecipeImpl.Builder<ItemStack>("recipe7")
                .brewDifficulty(10)
                .recipeResult(BukkitRecipeResult.GENERIC)
                .steps(
                        List.of(
                                new BrewingStep.Cook(
                                        new PassedMoment(10 * Moment.MINUTE),
                                        Map.of(
                                                new SimpleIngredient(Material.WHEAT), 10,
                                                new SimpleIngredient(Material.APPLE), 20
                                        ),
                                        CauldronType.WATER
                                ),
                                new BrewingStep.Distill(129),
                                new BrewingStep.Age(
                                        new PassedMoment(10 * Moment.AGING_YEAR),
                                        BarrelType.ACACIA
                                )
                        )
                )
                .build();
        Map<String, Recipe<ItemStack>> recipeMap = new HashMap<>();
        Stream.of(recipe1, recipe2, recipe3, recipe4, recipe5, recipe6, recipe7)
                .forEach(recipe -> recipeMap.put(recipe.getRecipeName(), recipe));
        registry.registerRecipes(recipeMap);
    }
}