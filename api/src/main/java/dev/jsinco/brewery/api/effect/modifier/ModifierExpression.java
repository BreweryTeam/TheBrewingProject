package dev.jsinco.brewery.api.effect.modifier;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.function.Functions;

import java.util.Map;

public record ModifierExpression(String function) {

    public static final ModifierExpression ZERO = new ModifierExpression("0");

    public double evaluate(Map<String, Double> variables) {
        ExpressionBuilder builder = new ExpressionBuilder(function);
        builder.variables(variables.keySet());
        builder.function(new Function("probabilityWeight", 1) {
            @Override
            public double apply(double... doubles) {
                return 0.04 / (110 - Math.max(0D, Math.min(doubles[0], 100D)));
            }
        });
        builder.function(new Function("max", 2) {
            @Override
            public double apply(double... doubles) {
                return Math.max(doubles[0], doubles[1]);
            }
        });
        builder.function(new Function("min", 2) {
            @Override
            public double apply(double... doubles) {
                return Math.min(doubles[0], doubles[1]);
            }
        });
        Expression expression = builder.build();
        for (Map.Entry<String, Double> entry : variables.entrySet()) {
            expression.setVariable(entry.getKey(), entry.getValue());
        }
        return expression.evaluate();
    }
}
