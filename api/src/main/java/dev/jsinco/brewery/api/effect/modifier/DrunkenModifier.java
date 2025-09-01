package dev.jsinco.brewery.api.effect.modifier;

public record DrunkenModifier(String name, ModifierExpression dependency, ModifierExpression decrementTime,
                              double minValue, double maxValue) {



    public double sanitize(double value) {
        return Math.max(minValue, Math.min(value, maxValue));
    }
}
