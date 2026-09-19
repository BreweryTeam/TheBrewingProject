package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeGroup;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Optional;

@NullMarked
public record RecipeGroupImpl<I>(String id, Component displayName,
                                 List<Recipe<I>> recipes) implements RecipeGroup<I> {
}
