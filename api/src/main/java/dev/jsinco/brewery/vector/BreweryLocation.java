package dev.jsinco.brewery.vector;

import java.util.UUID;

public record BreweryLocation(int x, int y, int z, UUID worldUuid) {

    public BreweryVector toVector() {
        return new BreweryVector(x, y, z);
    }

    public BreweryLocation add(int x, int y, int z) {
        return new BreweryLocation(x + x(), y + y(), z + z(), worldUuid);
    }

    public interface Supplier {

        BreweryLocation get();
    }
}
