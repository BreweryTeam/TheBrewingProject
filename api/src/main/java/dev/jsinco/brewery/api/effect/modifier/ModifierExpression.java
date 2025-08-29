package dev.jsinco.brewery.api.effect.modifier;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Map;
import java.util.stream.Collectors;

public record ModifierExpression(String function) {

    public static final ModifierExpression ZERO = new ModifierExpression("0");

    public double evaluate(Map<DrunkenModifier, Double> modifierValues) {
        ExpressionBuilder builder = new ExpressionBuilder(function);
        builder.variables(modifierValues.keySet().stream()
                .map(DrunkenModifier::name)
                .collect(Collectors.toSet()));
        Expression expression = builder.build();
        for (Map.Entry<DrunkenModifier, Double> entry : modifierValues.entrySet()) {
            expression.setVariable(entry.getKey().name(), entry.getValue());
        }
        return expression.evaluate();
    }
}
