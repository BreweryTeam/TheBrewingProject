package dev.jsinco.brewery.configuration.structure;

import dev.jsinco.brewery.api.breweries.BarrelType;
import eu.okaeri.configs.OkaeriConfig;

import java.util.Map;

public class BarrelTypeDefinition extends OkaeriConfig {

    private String name;

    private Map<String, Double> pairs = Map.of();

    public BarrelType toBarrelType() {
        
    }
}
