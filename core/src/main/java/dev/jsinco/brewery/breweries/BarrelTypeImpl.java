package dev.jsinco.brewery.breweries;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.util.BreweryKey;

import java.util.Map;

public class BarrelTypeImpl implements BarrelType {

    private final BreweryKey key;
    private final Map<BarrelType, Double> proximity;
    private final double backupProximity;

    public BarrelTypeImpl(BreweryKey key, Map<BarrelType, Double> proximity, double backupProximity) {
        this.key = key;
        this.proximity = proximity;
        this.backupProximity = backupProximity;
    }

    @Override
    public double proximityScore(BarrelType other) {
        Preconditions.checkNotNull(other);
        Double proximity = this.proximity.get(other);
        return proximity == null ? backupProximity : proximity;
    }

    @Override
    public BreweryKey key() {
        return key;
    }
}
