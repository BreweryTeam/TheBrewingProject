package dev.jsinco.brewery.api.effect.modifier;

public record DrunkenModifier(String name, ModifierExpression dependency, ModifierExpression decrementTime,
                              double defaultValue) {
}
