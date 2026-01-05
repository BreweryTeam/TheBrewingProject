package dev.jsinco.brewery.api.ingredient;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record IngredientGroup(String key, Component displayName, List<BaseIngredient> alternatives) implements ComplexIngredient {
    @Override
    public @NotNull String getKey() {
        return key;
    }

    @Override
    public List<BaseIngredient> derivatives() {
        return alternatives;
    }
}
