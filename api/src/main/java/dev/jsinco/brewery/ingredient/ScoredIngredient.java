package dev.jsinco.brewery.ingredient;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

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
