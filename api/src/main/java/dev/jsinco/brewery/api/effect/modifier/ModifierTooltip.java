package dev.jsinco.brewery.api.effect.modifier;

import dev.jsinco.brewery.api.brew.Brew;
import org.jetbrains.annotations.Nullable;

public record ModifierTooltip(ModifierExpression expression, @Nullable String brewingTooltip,
                              @Nullable String defaultTooltip, @Nullable String sealedTooltip) {
    public @Nullable String getTooltip(Brew.State state) {
        return switch (state) {
            case Brew.State.Brewing brewing -> brewingTooltip;
            case Brew.State.Other other -> defaultTooltip;
            case Brew.State.Seal seal -> sealedTooltip;
        };
    }
}
