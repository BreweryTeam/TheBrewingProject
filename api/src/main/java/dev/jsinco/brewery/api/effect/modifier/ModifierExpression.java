package dev.jsinco.brewery.api.effect.modifier;

import dev.jsinco.brewery.api.util.Logger;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import net.objecthunter.exp4j.function.Function;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record ModifierExpression(String function) {

    public static final ModifierExpression ZERO = new ModifierExpression("0");
    private static final Set<ModifierExpression> invalidExpressions = new HashSet<>();

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
        ValidationResult validationResult = expression.validate();
        if (!validationResult.isValid() && !invalidExpressions.contains(this)) {
            invalidExpressions.add(this);
            Logger.logWarn("Issue with modifier expression: " + function);
            validationResult.getErrors()
                    .forEach(Logger::logWarn);
            return 0D;
        }
        return expression.evaluate();
    }
}
