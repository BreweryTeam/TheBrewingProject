package dev.jsinco.brewery.api.effect.modifier;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Map;

public record ModifierExpression(String function) {

    public static final ModifierExpression ZERO = new ModifierExpression("0");

    public double evaluate(Map<String, Double> variables) {
        ExpressionBuilder builder = new ExpressionBuilder(function);
        builder.variables(variables.keySet());
        Expression expression = builder.build();
        for (Map.Entry<String, Double> entry : variables.entrySet()) {
            expression.setVariable(entry.getKey(), entry.getValue());
        }
        return expression.evaluate();
    }
}
