package dev.jsinco.brewery.api.ingredient;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public record IngredientGroup(String key, Component displayName,
                              List<Ingredient> alternatives) implements ComplexIngredient {
    @Override
    public @NotNull String getKey() {
        return key;
    }

    @Override
    public List<BaseIngredient> derivatives() {
        return alternatives
                .stream()
                .flatMap(alternative -> {
                    if (alternative instanceof ComplexIngredient complexIngredient) {
                        return complexIngredient.derivatives().stream();
                    }
                    return alternative instanceof BaseIngredient baseIngredient ? Stream.of(baseIngredient) : Stream.empty();
                })
                .toList();
    }
}
