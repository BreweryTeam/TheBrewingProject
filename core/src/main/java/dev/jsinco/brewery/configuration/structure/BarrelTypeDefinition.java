package dev.jsinco.brewery.configuration.structure;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.BarrelTypeProvider;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Logger;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BarrelTypeDefinition extends OkaeriConfig {

    @CustomKey("name")
    private String name = null;

    @CustomKey("proximity-multipliers")
    private Map<String, Double> proximityMultipliers = Map.of();

    @CustomKey("default-proximity-multipliers")
    private double defaultProximity = 0.7D;

    public Optional<BarrelType> toBarrelType() {
        if (name == null) {
            return Optional.empty();
        }
        BarrelType.Builder builder = BarrelTypeProvider.builderStatic(name);
        proximityMultipliers.entrySet().stream()
                .filter(entry -> inBounds(entry.getValue()))
                .forEach(entry -> builder.addProximity(BreweryKey.parse(entry.getKey()), entry.getValue()));
        return Optional.of(
                builder.defaultProximity(inBounds(defaultProximity) ? defaultProximity : 1D)
                        .build()
        );
    }

    public void postValidate(List<BarrelType> barrelTypes) {
        if (name == null) {
            Logger.logErr("Barrel type name was null, skipping...");
        }
        for (Map.Entry<String, Double> entry : proximityMultipliers.entrySet()) {
            BreweryKey key = BreweryKey.parse(entry.getKey());
            if (barrelTypes.stream().map(BarrelType::key).noneMatch(key::equals)) {
                Logger.logWarn("Invalid barrel type '" + name + "' unknown proximity multiplier barrel type: " + key.minimalized());
            }
            if (!inBounds(entry.getValue())) {
                Logger.logWarn("Invalid barrel type '" + name + "', wrong proximity multiplier number out of range [0, 1]: " + key.minimalized());
            }
        }
        if (!inBounds(defaultProximity)) {
            Logger.logWarn("Invalid barrel type, default proximity multiplier out of range [0, 1]: " + name);
        }
    }

    private boolean inBounds(double value) {
        return value >= 0 && value <= 1;
    }
}
