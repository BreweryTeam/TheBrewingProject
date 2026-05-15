package dev.jsinco.brewery.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

public class RegistryProviderHolder {

    private static RegistryProvider instance;

    @ApiStatus.Internal
    public static RegistryProvider instance() {
        if (instance == null) {
            instance = ServiceLoader.load(RegistryProvider.class, RegistryProvider.class.getClassLoader()).findFirst()
                    .orElseThrow();
        }
        return instance;
    }
}
