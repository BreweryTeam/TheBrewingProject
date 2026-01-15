package dev.jsinco.brewery.api.ingredient;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @param baseIngredient The underlying ingredient for this
 * @param score          The score of the ingredient
 */
@Deprecated(forRemoval = true)
public record ScoredIngredient(Ingredient baseIngredient, double score) implements Ingredient {

    @Override
    public @NotNull String getKey() {
        return baseIngredient.getKey();
    }

    @Override
    public @NotNull Component displayName() {
        return baseIngredient.displayName()
                .append(Component.text("-"))
                .append(Component.text(score));
    }

    @Override
    public Optional<? extends Ingredient> findMatch(Set<BaseIngredient> baseIngredientSet) {
        return baseIngredient.findMatch(baseIngredientSet)
                .map(ingredient -> new IngredientWithMeta(ingredient, Map.of(IngredientMeta.SCORE, score)));
    }

    @Override
    public BaseIngredient toBaseIngredient() {
        return baseIngredient.toBaseIngredient();
    }
}
