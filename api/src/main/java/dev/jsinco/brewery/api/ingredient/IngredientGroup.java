package dev.jsinco.brewery.api.ingredient;

import dev.jsinco.brewery.api.util.BreweryKey;
import net.kyori.adventure.text.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record IngredientGroup(BreweryKey key, Component displayName,
                              List<Ingredient> alternatives) implements Ingredient {

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
