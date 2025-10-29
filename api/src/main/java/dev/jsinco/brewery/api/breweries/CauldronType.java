package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum CauldronType implements BreweryKeyed {

    WATER("minecraft:water_cauldron"),
    LAVA("minecraft:lava_cauldron"),
    SNOW("minecraft:powder_snow_cauldron");

    private String materialKey;


    CauldronType(String materialKey) {
        this.materialKey = materialKey;
    }

    public String materialKey() {
        return materialKey;
    }

    public BreweryKey key() {
        return BreweryKey.parse(name());
    }

    public static @Nullable CauldronType from(String materialType) {
        for (CauldronType cauldronType : BreweryRegistry.CAULDRON_TYPE.values()) {
            if (cauldronType.materialKey().equals(materialType)) {
                return cauldronType;
            }
        }
        return null;
    }
}
