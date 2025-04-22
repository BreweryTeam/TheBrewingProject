package dev.jsinco.brewery.brew;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.recipe.RecipeRegistry;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BrewImpl implements Brew {

    @Getter
    private final List<BrewingStep> steps;
    public static final BrewSerializer SERIALIZER = new BrewSerializer();

    public BrewImpl(BrewingStep.Cook cook) {
        this(List.of(cook));
    }

    public BrewImpl(BrewingStep.Mix mix) {
        this(List.of(mix));
    }

    public BrewImpl(@NotNull List<BrewingStep> steps) {
        Preconditions.checkArgument(!steps.isEmpty(), "Steps cannot be empty");
        this.steps = steps;
    }

    public BrewImpl withStep(BrewingStep step) {
        return new BrewImpl(Stream.concat(steps.stream(), Stream.of(step)).toList());
    }

    public BrewImpl witModifiedLastStep(Function<BrewingStep, BrewingStep> modifier) {
        BrewingStep newStep = modifier.apply(steps.getLast());
        return new BrewImpl(Stream.concat(
                steps.subList(0, steps.size() - 1).stream(),
                Stream.of(newStep)).toList()
        );
    }

    public <B extends BrewingStep> BrewImpl withLastStep(Class<B> bClass, Function<B, B> modifier, Supplier<B> stepSupplier) {
        if (bClass.isInstance(lastStep())) {
            BrewingStep newStep = modifier.apply(bClass.cast(lastStep()));
            return new BrewImpl(Stream.concat(
                    steps.subList(0, steps.size() - 1).stream(),
                    Stream.of(newStep)).toList()
            );
        }
        return withStep(stepSupplier.get());
    }

    public <I> Optional<Recipe<I>> closestRecipe(RecipeRegistry<I> registry) {
        double bestScore = 0;
        Recipe<I> bestMatch = null;
        for (Recipe<I> recipe : registry.getRecipes()) {
            double score = score(recipe).rawScore();
            if (score > bestScore) {
                bestScore = score;
                bestMatch = recipe;
            }
        }
        return Optional.ofNullable(bestMatch);
    }

    public @NotNull BrewScore score(Recipe<?> recipe) {
        List<BrewingStep> recipeSteps = recipe.getSteps();
        List<Double> scores = new ArrayList<>();
        if (steps.size() > recipeSteps.size()) {
            return BrewScoreImpl.NONE;
        }
        for (int i = 0; i < steps.size(); i++) {
            BrewingStep recipeStep = recipeSteps.get(i);
            scores.add(recipeStep.proximity(steps.get(i)));
        }
        boolean completed = steps.size() == recipeSteps.size();
        BrewScoreImpl brewScore = new BrewScoreImpl(scores, completed, recipe.getBrewDifficulty());
        if (brewScore.brewQuality() == null) {
            scores.removeLast();
            scores.add(recipeSteps.get(steps.size() - 1).maximumScore(steps.getLast()));
            return new BrewScoreImpl(scores, false, recipe.getBrewDifficulty());
        }
        return brewScore;
    }

    public Optional<BrewQuality> quality(Recipe<?> recipe) {
        return Optional.ofNullable(score(recipe).brewQuality());
    }

    public BrewingStep lastStep() {
        return steps.getLast();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BrewImpl brew = (BrewImpl) other;
        return steps.equals(brew.steps);
    }

}
