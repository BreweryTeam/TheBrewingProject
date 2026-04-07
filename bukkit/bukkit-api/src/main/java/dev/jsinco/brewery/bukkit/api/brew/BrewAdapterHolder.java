package dev.jsinco.brewery.bukkit.api.brew;

import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

@ApiStatus.Internal
public class BrewAdapterHolder {

    private static BrewAdapter instance;

    @ApiStatus.Internal
    public static BrewAdapter instance() {
        if (instance == null) {
            instance = ServiceLoader.load(BrewAdapter.class, BrewAdapter.class.getClassLoader()).findFirst()
                    .orElseThrow();
        }
        return instance;
    }
}
