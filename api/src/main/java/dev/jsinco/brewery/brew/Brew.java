package dev.jsinco.brewery.brew;

import com.google.errorprone.annotations.Immutable;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.recipe.RecipeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Immutable
public interface Brew {

    <I> Optional<Recipe<I>> closestRecipe(RecipeRegistry<I> registry);

    @NotNull BrewScore score(Recipe<?> recipe);

    Optional<BrewQuality> quality(Recipe<?> recipe);

    BrewingStep lastCompletedStep();

    BrewingStep lastStep();

    Brew withStep(BrewingStep step);

    Brew witModifiedLastStep(Function<BrewingStep, BrewingStep> modifier);

    <B extends BrewingStep> Brew withLastStep(Class<B> bClass, Function<B, B> modifier, Supplier<B> stepSupplier);

    List<BrewingStep> getCompletedSteps();

    List<BrewingStep> getSteps();

    sealed interface State {

        record Brewing() implements State {
        }

        record Other() implements State {
        }

        record Seal(@Nullable String message) implements State {
        }
    }
}
