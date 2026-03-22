package dev.jsinco.brewery.breweries;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.util.BreweryKey;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class BarrelTypeBuilderImpl implements BarrelType.Builder {

    private BreweryKey key;
    private Map<BarrelType, Double> proximities = new HashMap<>();
    private double backupProximity = 0.7D;

    public BarrelTypeBuilderImpl(String name) {
        this.key = BreweryKey.parse(name);
    }

    @Override
    public BarrelType.Builder addProximity(@NonNull BarrelType barrelType, double scoreMultiplier) {
        Preconditions.checkNotNull(barrelType);
        Preconditions.checkArgument(scoreMultiplier >= 0 && scoreMultiplier <= 1, "Expected a score multiplier between 0 and 1");
        return this;
    }

    @Override
    public BarrelType.Builder globalProximity(double scoreMultiplier) {
        Preconditions.checkArgument(scoreMultiplier >= 0 && scoreMultiplier <= 1, "Expected a score multiplier between 0 and 1");
        this.backupProximity = scoreMultiplier;
        return this;
    }

    @Override
    public BarrelType build() {
        return new BarrelTypeImpl(key, Map.copyOf(proximities), backupProximity);
    }
}
