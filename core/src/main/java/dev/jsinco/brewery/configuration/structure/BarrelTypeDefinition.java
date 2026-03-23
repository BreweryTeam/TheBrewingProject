package dev.jsinco.brewery.configuration.structure;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.BarrelTypeProvider;
import dev.jsinco.brewery.api.util.BreweryKey;
import me.sparky983.warp.Configuration;
import me.sparky983.warp.Property;

import java.util.Map;

@Configuration
public interface BarrelTypeDefinition {

    @Property("name")
    String name();

    @Property("proximity-multipliers")
    Map<String, Double> proximityMultipliers();

    @Property("default-proximity-multipliers")
    double defaultProximity();

    default BarrelType toBarrelType() {
        BarrelType.Builder builder = BarrelTypeProvider.builderStatic(name());
        proximityMultipliers().forEach((key, value) -> builder.addProximity(BreweryKey.parse(key), value));
        return builder.defaultProximity(defaultProximity())
                .build();
    }
}
