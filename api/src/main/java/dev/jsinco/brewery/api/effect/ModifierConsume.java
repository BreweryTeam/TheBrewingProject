package dev.jsinco.brewery.api.effect;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;

public record ModifierConsume(DrunkenModifier modifier, double value, boolean cascade) {

    public ModifierConsume(DrunkenModifier modifier, double value) {
        this(modifier, value, false);
    }
}
