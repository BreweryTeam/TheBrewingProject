package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.BreweryKeyed;
import dev.jsinco.brewery.util.Registry;
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
        return BreweryKey.parse(name().toLowerCase(Locale.ROOT));
    }

    public static @Nullable CauldronType from(String materialType) {
        for (CauldronType cauldronType : Registry.CAULDRON_TYPE.values()) {
            if (cauldronType.materialKey().equals(materialType)) {
                return cauldronType;
            }
        }
        return null;
    }
}
