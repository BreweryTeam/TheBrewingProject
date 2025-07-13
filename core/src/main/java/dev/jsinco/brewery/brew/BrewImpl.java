package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.configuration.Config;
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
        if (!steps.isEmpty() && bClass.isInstance(lastStep())) {
            BrewingStep newStep = modifier.apply(bClass.cast(lastStep()));
            return new BrewImpl(Stream.concat(
                    steps.subList(0, steps.size() - 1).stream(),
                    Stream.of(newStep)).toList()
            );
        }
        return withStep(stepSupplier.get());
    }

    @Override
    public List<BrewingStep> getCompletedSteps() {
        return steps.stream()
                .filter(this::isCompleted)
                .toList();
    }

    private boolean isCompleted(BrewingStep step) {
        return !(step instanceof BrewingStep.Age age) || age.time().moment() > Config.config().barrels().agingYearTicks() / 2;
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
        List<List<PartialBrewScore>> scores = new ArrayList<>();
        List<BrewingStep> completedSteps = getCompletedSteps();
        if (completedSteps.size() > recipeSteps.size()) {
            return BrewScoreImpl.failed(this);
        }
        for (int i = 0; i < completedSteps.size(); i++) {
            BrewingStep recipeStep = recipeSteps.get(i);
            scores.add(recipeStep.proximityScores(completedSteps.get(i)));
        }
        boolean completed = completedSteps.size() == recipeSteps.size();
        BrewScoreImpl brewScore = new BrewScoreImpl(scores, completed, recipe.getBrewDifficulty());
        if (brewScore.brewQuality() == null) {
            scores.removeLast();
            scores.add(recipeSteps.get(completedSteps.size() - 1).maximumScores(completedSteps.getLast()));
            BrewScoreImpl uncompleted = new BrewScoreImpl(scores, false, recipe.getBrewDifficulty());
            if (uncompleted.brewQuality() != null) {
                return uncompleted;
            }
        }
        return brewScore;
    }

    public Optional<BrewQuality> quality(Recipe<?> recipe) {
        return Optional.ofNullable(score(recipe).brewQuality());
    }

    @Override
    public @NotNull BrewingStep lastCompletedStep() {
        for (int i = steps.size() - 1; i >= 0; i--) {
            BrewingStep step = steps.get(i);
            if (isCompleted(step)) {
                return step;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public @NotNull BrewingStep lastStep() {
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
