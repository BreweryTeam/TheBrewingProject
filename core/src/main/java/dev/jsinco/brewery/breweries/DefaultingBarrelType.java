package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.structure.BarrelTypeDefinition;
import org.jspecify.annotations.Nullable;

public class DefaultingBarrelType implements BarrelType {

    private final BarrelType defaultValue;
    private @Nullable BarrelType override = null;
    private boolean dirty = true;

    public DefaultingBarrelType(BarrelType defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public double proximityScore(BarrelType other) {
        if (dirty) {
            Config.config().barrels().customBarrels().stream()
                    .map(BarrelTypeDefinition::toBarrelType)
                    .filter(barrelType -> barrelType.key().equals(defaultValue.key()))
                    .filter(barrelType -> !(barrelType instanceof DefaultingBarrelType))
                    .findAny()
                    .ifPresent(barrelType -> override = barrelType);
            dirty = false;
        }
        BarrelType barrelType = override == null ? defaultValue : override;
        return barrelType.proximityScore(override);
    }

    @Override
    public BreweryKey key() {
        return defaultValue.key();
    }
}
