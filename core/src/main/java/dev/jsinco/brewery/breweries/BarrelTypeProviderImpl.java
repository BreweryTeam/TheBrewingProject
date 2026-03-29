package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.BarrelTypeProvider;
import dev.jsinco.brewery.configuration.structure.BarrelTypeDefinitions;

import java.util.Collection;

public class BarrelTypeProviderImpl implements BarrelTypeProvider {

    @Override
    public BarrelType.Builder builder(String name) {
        return new BarrelTypeBuilderImpl(name);
    }

    @Override
    public Collection<BarrelType> allBarrels() {
        return BarrelTypeDefinitions.allBarrelTypes();
    }
}
