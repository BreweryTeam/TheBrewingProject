package dev.jsinco.brewery.bukkit.recipe;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeMatcher;
import dev.jsinco.brewery.api.recipe.RecipeMatcherResult;
import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Set;

public record RecipeMatcherImpl(@Nullable Set<Recipe<ItemStack>> whitelist,
                                boolean disallowStepVariations) implements RecipeMatcher<ItemStack> {

    @Override
    public RecipeMatcherResult<ItemStack> match(Brew brew) {
        RecipeRegistry<ItemStack> registry = TheBrewingProject.getInstance().getRecipeRegistry();
        return brew.variations()
                .stream()
                .flatMap(variation -> registry.possibleRecipes(variation)
                        .stream()
                        .filter(recipe -> whitelist == null || whitelist.contains(recipe))
                        .map(recipe ->
                                new RecipeMatcherResultImpl(
                                        recipe,
                                        variation,
                                        brew,
                                        recipe.score(variation)
                                ))
                )
                .filter(pair -> pair.score().rawScore() > 0)
                .max(Comparator.comparing(RecipeMatcherResult::score))
                .orElseGet(() -> new RecipeMatcherResultImpl(
                        null,
                        brew.getCompletedSteps(),
                        brew,
                        BrewScoreImpl.failed(brew.getCompletedSteps())
                ));
    }

    public static BuilderImpl builder() {
        return new BuilderImpl();
    }

    public static class BuilderImpl implements RecipeMatcher.Builder<ItemStack> {

        private Set<Recipe<ItemStack>> recipeWhitelist = null;
        private boolean disallowStepVariations = false;

        @Override
        public Builder<ItemStack> recipeWhitelist(Set<Recipe<ItemStack>> recipes) {
            this.recipeWhitelist = Set.copyOf(recipes);
            return this;
        }

        @Override
        public Builder<ItemStack> matchAgainstOnly(Recipe<ItemStack> recipe) {
            this.recipeWhitelist = Set.of(recipe);
            return this;
        }

        @Override
        public Builder<ItemStack> disallowStepVariations() {
            this.disallowStepVariations = true;
            return this;
        }

        @Override
        public RecipeMatcher<ItemStack> build() {
            return new RecipeMatcherImpl(
                    recipeWhitelist,
                    disallowStepVariations
            );
        }
    }
}
