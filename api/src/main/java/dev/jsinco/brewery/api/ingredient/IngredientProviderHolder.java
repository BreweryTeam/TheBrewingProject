package dev.jsinco.brewery.api.ingredient;

import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

public class IngredientProviderHolder {

    private static IngredientProvider instance;

    @ApiStatus.Internal
    public static IngredientProvider instance() {
        if (instance == null) {
            instance = ServiceLoader.load(IngredientProvider.class, IngredientProvider.class.getClassLoader()).findFirst()
                    .orElseThrow();
        }
        return instance;
    }
}
