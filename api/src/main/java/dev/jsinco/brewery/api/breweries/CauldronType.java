package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import org.jspecify.annotations.Nullable;

public enum CauldronType implements BreweryKeyed {

    WATER("minecraft:water_cauldron", true),
    LAVA("minecraft:lava_cauldron", true),
    SNOW("minecraft:powder_snow_cauldron", true),
    BREW("minecraft:water_cauldron", false);

    private String materialKey;
    private boolean legacyValue;


    CauldronType(String materialKey, boolean legacyValue) {
        this.materialKey = materialKey;
        this.legacyValue = legacyValue;
    }

    @Deprecated
    public String materialKey() {
        return materialKey;
    }

    public BreweryKey key() {
        return BreweryKey.parse(name());
    }

    @Deprecated
    public static @Nullable CauldronType from(String materialType) {
        for (CauldronType cauldronType : BreweryRegistry.CAULDRON_TYPE.values()) {
            if (cauldronType.legacyValue && cauldronType.materialKey() != null && cauldronType.materialKey().equals(materialType)) {
                return cauldronType;
            }
        }
        return null;
    }

    public boolean appliesTo(CauldronType otherType) {
        return otherType == this;
    }
}
