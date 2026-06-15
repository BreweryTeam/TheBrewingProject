package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.PartialBrewScore;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.ingredient.BaseIngredient;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientMeta;
import dev.jsinco.brewery.api.ingredient.IngredientProviderHolder;
import dev.jsinco.brewery.api.ingredient.IngredientWithMeta;
import dev.jsinco.brewery.api.recipe.QualityData;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import dev.jsinco.brewery.api.util.BreweryKey;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Override
    public Ingredient toIngredient(double score) {
        BaseIngredient base = IngredientProviderHolder.instance()
                .breweryIngredient(BreweryKey.parse(recipeName));
        BrewQuality quality = BrewQuality.quality(score)
                .orElseThrow(() -> new IllegalArgumentException("0 valued scores are not allowed"));
        return new IngredientWithMeta(base,
                Map.of(
                        IngredientMeta.SCORE, score,
                        IngredientMeta.DISPLAY_NAME, getRecipeResult(quality).displayName()
                )
        );
    }

    @Override
    public BrewScore score(List<BrewingStep> brewingSteps) {
        if (brewingSteps.size() > steps.size()) {
            return BrewScoreImpl.failed(brewingSteps);
        }
        List<Map<ScoreType, PartialBrewScore>> scores = new ArrayList<>();
        for (int i = 0; i < brewingSteps.size(); i++) {
            BrewingStep recipeStep = steps.get(i);
            scores.add(recipeStep.proximityScores(brewingSteps.get(i)));
        }
        boolean completed = steps.size() == brewingSteps.size();
        BrewScoreImpl brewScore = new BrewScoreImpl(scores, completed, getBrewDifficulty());
        if (brewScore.brewQuality() == null) {
            scores.removeLast();
            scores.add(steps.get(brewingSteps.size() - 1).maximumScores(brewingSteps.getLast()));
            BrewScoreImpl uncompleted = new BrewScoreImpl(scores, false, getBrewDifficulty());
            if (uncompleted.brewQuality() != null) {
                return uncompleted;
            }
        }
        return brewScore;
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
