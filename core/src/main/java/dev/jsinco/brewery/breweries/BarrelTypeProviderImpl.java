package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.BarrelTypeProvider;

public class BarrelTypeProviderImpl implements BarrelTypeProvider {
    @Override
    public BarrelType.Builder builder(String name) {
        return new BarrelTypeBuilderImpl(name);
    }

    @Override
    public BarrelType predefined(String name) {
        return null;
    }
}
