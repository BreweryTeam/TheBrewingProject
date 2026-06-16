package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.brew.Brew;

import java.util.Set;

public interface RecipeMatcher<I> {

    /**
     * Match against a brew.
     *
     * @param brew A brew to match against
     * @return The result of the match
     */
    RecipeMatcherResult<I> match(Brew brew);


    interface Builder<I> {

        /**
         * @param recipes The recipes to match against
         * @return This builder
         */
        Builder<I> recipeWhitelist(Set<Recipe<I>> recipes);

        /**
         * @param recipe One recipe to match against
         * @return This builder
         */
        Builder<I> matchAgainstOnly(Recipe<I> recipe);

        /**
         * Disallow step variations, do not check for alternate routes
         *
         * @return This builder
         */
        Builder<I> disallowStepVariations();

        /**
         * @return A new recipe matcher
         */
        RecipeMatcher<I> build();
    }
}
