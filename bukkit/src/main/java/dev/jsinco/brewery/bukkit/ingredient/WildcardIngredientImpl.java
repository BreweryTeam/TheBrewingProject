package dev.jsinco.brewery.bukkit.ingredient;

import dev.jsinco.brewery.api.ingredient.IngredientInput;
import dev.jsinco.brewery.api.ingredient.UncheckedIngredient;
import dev.jsinco.brewery.api.ingredient.WildcardIngredient;

import java.util.Arrays;

public record WildcardIngredientImpl(String value) implements WildcardIngredient {
    @Override
    public boolean matches(IngredientInput other) {
        if (!(other instanceof UncheckedIngredient uncheckedIngredient)) {
            return false;
        }
        return matches(value.split(":"), uncheckedIngredient.key().toString().split(":"));
    }

    public static boolean matches(String[] wildcardArgument, String[] key) {
        int i2 = 0;
        for (int i1 = 0; i1 < wildcardArgument.length; i1++) {
            if (key.length <= i2) {
                return false;
            }
            if (!wildcardArgument[i1].equals("*")) {
                if (!wildcardArgument[i1].equals(key[i2])) {
                    return false;
                }
                i2++;
                continue;
            }
            while (i2 < key.length) {
                if (i1 >= wildcardArgument.length + 1 || i2 >= key.length + 1) {
                    return false;
                }
                if (matches(Arrays.copyOfRange(wildcardArgument, i1 + 1, wildcardArgument.length), Arrays.copyOfRange(key, i2 + 1, key.length))) {
                    return true;
                }
                i2++;
            }
        }
        return true;
    }
}
