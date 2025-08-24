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

    /**
     * @param registry A registry with all available recipes
     * @param <I>      Item stack type
     * @return The closest recipe, if any are not fatally different
     */
    <I> Optional<Recipe<I>> closestRecipe(RecipeRegistry<I> registry);

    /**
     * @param recipe The recipe to calculate the score on
     * @return The score on this brew for the spcecified score
     */
    @NotNull BrewScore score(Recipe<?> recipe);

    /**
     * @param recipe The recipe to calculate the quality on
     * @return The quality for this recipe, if the brew is not failed
     */
    Optional<BrewQuality> quality(Recipe<?> recipe);

    /**
     * @return The last completed step
     */
    BrewingStep lastCompletedStep();

    /**
     * This includes uncompleted steps, which can be for example aging under 0.5 aging years
     *
     * @return The last step
     */
    BrewingStep lastStep();

    /**
     * @param step A brewing step
     * @return A new brew instance with the brewing step added in the chain
     */
    Brew withStep(BrewingStep step);

    /**
     * @param modifier Function that modifies the last step
     * @return A new brew instance with the modified last step
     */
    Brew witModifiedLastStep(Function<BrewingStep, BrewingStep> modifier);

    /**
     * @param bClass       The type of the brewing step
     * @param modifier     The modifier of the brewing step
     * @param stepSupplier A supplier of new brewing step
     * @param <B>          The type of the brewing step
     * @return A new brewing step with the applied modifier if already existed with type, or a new step from the step supplier
     */
    <B extends BrewingStep> Brew withLastStep(Class<B> bClass, Function<B, B> modifier, Supplier<B> stepSupplier);

    /**
     * @return All completed steps
     */
    List<BrewingStep> getCompletedSteps();

    /**
     * This includes uncompleted steps, which can be for example aging under 0.5 aging years
     *
     * @return All steps
     */
    List<BrewingStep> getSteps();

    /**
     * A state of a brew, mainly indicates how the data should be written when converting into an item
     */
    sealed interface State {

        /**
         * Is in a brewing process, show most info
         */
        record Brewing() implements State {
        }

        /**
         * Usually in a player inventory or other inventory
         */
        record Other() implements State {
        }

        /**
         * Sealed, and final
         *
         * @param message The message to display for the sealed brew
         */
        record Seal(@Nullable String message) implements State {
        }
    }
}
