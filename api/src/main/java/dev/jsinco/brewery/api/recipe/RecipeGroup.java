package dev.jsinco.brewery.api.recipe;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Optional;

@NullMarked
public interface RecipeGroup<I> {

    /**
     * @return the id of this recipe group
     */
    String id();

    /**
     * @return the display name representing this recipe group
     */
    Optional<Component> displayName();

    /**
     * @return all recipes linked to this recipe group
     */
    List<Recipe<I>> recipes();
}
