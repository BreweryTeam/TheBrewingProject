package dev.jsinco.brewery.api.ingredient;

import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

public class IngredientInputProviderHolder {

    private static IngredientInputProvider instance;

    @ApiStatus.Internal
    public static IngredientInputProvider instance() {
        if (instance == null) {
            instance = ServiceLoader.load(IngredientInputProvider.class, IngredientInputProvider.class.getClassLoader()).findFirst()
                    .orElseThrow();
        }
        return instance;
    }
}
