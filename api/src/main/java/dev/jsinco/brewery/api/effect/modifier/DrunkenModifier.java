package dev.jsinco.brewery.api.effect.modifier;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

public record DrunkenModifier(String name, @Nullable ModifierExpression dependency,
                              @Nullable ModifierExpression decrementTime,
                              double minValue, double maxValue, Component displayName) {


    public double sanitize(double value) {
        return Math.max(minValue, Math.min(value, maxValue));
    }
}
