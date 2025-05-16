package dev.jsinco.brewery.event;

import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.BreweryKeyed;

import java.util.Locale;

public enum NamedDrunkEvent implements DrunkEvent, BreweryKeyed {
    PUKE(45, 45, 40),
    PASS_OUT(80, 80, 10),
    STUMBLE(25, 0, 100),
    CHICKEN(99, 50, 2),
    TELEPORT(90, 40, 14),
    DRUNK_MESSAGE(25, 0, 30),
    NAUSEA(60, 50, 50),
    DRUNKEN_WALK(60, 20, 20);


    private final int alcoholRequirement;
    private final int toxinsRequirement;
    private final int probabilityWeight;
    private final BreweryKey key;

    NamedDrunkEvent(int alcoholRequirement, int toxinsRequirement, int probabilityWeight) {
        this.alcoholRequirement = alcoholRequirement;
        this.toxinsRequirement = toxinsRequirement;
        this.probabilityWeight = probabilityWeight;
        this.key = BreweryKey.parse(this.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public int alcoholRequirement() {
        return alcoholRequirement;
    }

    @Override
    public int toxinsRequirement() {
        return toxinsRequirement;
    }

    public BreweryKey key() {
        return this.key;
    }

    @Override
    public String displayName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public int probabilityWeight() {
        return probabilityWeight;
    }
}
