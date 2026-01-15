package dev.jsinco.brewery.api.ingredient;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public record IngredientGroup(String key, Component displayName,
                              List<Ingredient> alternatives) implements Ingredient {
    @Override
    public @NotNull String getKey() {
        return key;
    }

    @Override
    public Optional<? extends Ingredient> findMatch(Set<BaseIngredient> baseIngredientSet) {
        return alternatives.stream()
                .map(ingredient -> ingredient.findMatch(baseIngredientSet))
                .flatMap(Optional::stream)
                .max(Comparator.comparing(ingredient ->
                        ingredient instanceof IngredientWithMeta ingredientWithMeta && ingredientWithMeta.get(IngredientMeta.SCORE) instanceof Double score ? score : 1D)
                );
    }

    @Override
    public BaseIngredient toBaseIngredient() {
        return alternatives.getFirst().toBaseIngredient();
    }
}
