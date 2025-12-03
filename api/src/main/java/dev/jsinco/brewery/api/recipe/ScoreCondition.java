package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ScoreCondition {

    boolean matches(@Nullable BrewingStep expected, BrewingStep actual);

    interface SingletonCondition extends ScoreCondition {

        AmountCondition amountCondition();

        ScoreType type();
    }

    interface IngredientsCondition extends ScoreCondition {

        Map<Ingredient, AmountCondition> conditions();
    }

    enum AmountCondition {
        EXCESSIVE,
        LACKING,
        ANY;

        public boolean matches(double value, double expected) {
            if (this == ANY) {
                return true;
            }
            if (expected == -1D) {
                return false;
            }
            if (this == LACKING) {
                return value < expected;
            }
            return value > expected;
        }
    }
}
