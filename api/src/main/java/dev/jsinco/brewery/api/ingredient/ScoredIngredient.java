package dev.jsinco.brewery.api.ingredient;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * @param baseIngredient The underlying ingredient for this
 * @param score          The score of the ingredient
 */
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
}
