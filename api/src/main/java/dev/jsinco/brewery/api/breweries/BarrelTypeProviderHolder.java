package dev.jsinco.brewery.api.breweries;

import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

@ApiStatus.Internal
public class BarrelTypeProviderHolder {

    private static BarrelTypeProvider instance;

    static BarrelTypeProvider instance() {
        if (instance == null) {
            instance = ServiceLoader.load(BarrelTypeProvider.class, BarrelTypeProvider.class.getClassLoader()).findFirst()
                    .orElseThrow();
        }
        return instance;
    }
}
