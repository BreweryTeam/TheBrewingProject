package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum CauldronType implements BreweryKeyed {

    WATER("minecraft:water_cauldron"),
    LAVA("minecraft:lava_cauldron"),
    SNOW("minecraft:powder_snow_cauldron"),
    BREW(),
    SOLUTION(BREW, WATER);

    private String materialKey;
    private boolean legacyValue;
    private Set<CauldronType> allowedVariations;


    CauldronType(String materialKey) {
        this.materialKey = materialKey;
        this.legacyValue = true;
        allowedVariations = Set.of();
    }

    CauldronType(CauldronType... alternatives) {
        this.materialKey = "minecraft:water_cauldron";
        this.legacyValue = false;
        this.allowedVariations = Arrays.stream(alternatives).collect(Collectors.toSet());
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
        if (otherType == this) {
            return true;
        }
        return allowedVariations.contains(otherType);
    }
}
