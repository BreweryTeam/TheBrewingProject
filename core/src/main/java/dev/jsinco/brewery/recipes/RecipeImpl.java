package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.recipe.QualityData;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class RecipeImpl<I> implements Recipe<I> {

    private final String recipeName;
    private final double brewDifficulty;
    @NonNull
    private final List<BrewingStep> steps;
    @NonNull
    private final QualityData<RecipeResult<I>> recipeResults;

    private RecipeImpl(String recipeName, double brewDifficulty, List<BrewingStep> steps,
                       @NonNull QualityData<RecipeResult<I>> recipeResults) {
        this.recipeName = recipeName;
        this.brewDifficulty = brewDifficulty;
        this.steps = steps;
        this.recipeResults = recipeResults;
    }

    public @NonNull List<BrewingStep> getSteps() {
        return this.steps;
    }

    public @NonNull QualityData<RecipeResult<I>> getRecipeResults() {
        return this.recipeResults;
    }

    public String getRecipeName() {
        return this.recipeName;
    }

    public double getBrewDifficulty() {
        return this.brewDifficulty;
    }

    public static class Builder<I> {
        private final String recipeName;
        private double brewDifficulty = 1;
        private QualityData<RecipeResult<I>> recipeResult;
        private List<BrewingStep> steps;

        public Builder(String recipeName) {
            this.recipeName = recipeName;
        }

        public Builder<I> brewDifficulty(double brewDifficulty) {
            this.brewDifficulty = brewDifficulty;
            return this;
        }

        public Builder<I> recipeResults(@NonNull QualityData<RecipeResult<I>> recipeResult) {
            this.recipeResult = Preconditions.checkNotNull(recipeResult);
            return this;
        }

        public Builder<I> steps(@NonNull List<BrewingStep> steps) {
            this.steps = Preconditions.checkNotNull(steps);
            return this;
        }

        public RecipeImpl<I> build() {
            Preconditions.checkNotNull(recipeResult);
            Preconditions.checkNotNull(steps);
            if (steps.isEmpty()) {
                throw new IllegalStateException("Steps should not be empty");
            }
            return new RecipeImpl<>(recipeName, brewDifficulty, steps, recipeResult);
        }
    }
}
