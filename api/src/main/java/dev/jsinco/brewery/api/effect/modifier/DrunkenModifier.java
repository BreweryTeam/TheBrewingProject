package dev.jsinco.brewery.api.effect.modifier;

import net.kyori.adventure.text.Component;

public record DrunkenModifier(String name, ModifierExpression dependency, ModifierExpression decrementTime,
                              double minValue, double maxValue, Component displayName) {


    public double sanitize(double value) {
        return Math.max(minValue, Math.min(value, maxValue));
    }
}
