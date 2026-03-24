package dev.jsinco.brewery.configuration.structure;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.BarrelTypeProvider;
import dev.jsinco.brewery.api.util.BreweryKey;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;

import java.util.Map;

public class BarrelTypeDefinition extends OkaeriConfig {

    @CustomKey("name")
    public final String name = null;

    @CustomKey("proximity-multipliers")
    public final Map<String, Double> proximityMultipliers = Map.of();

    @CustomKey("default-proximity-multipliers")
    public final double defaultProximity = 0.7D;

    public BarrelType toBarrelType() {
        BarrelType.Builder builder = BarrelTypeProvider.builderStatic(name);
        proximityMultipliers.forEach((key, value) -> builder.addProximity(BreweryKey.parse(key), value));
        return builder.defaultProximity(defaultProximity)
                .build();
    }
}
