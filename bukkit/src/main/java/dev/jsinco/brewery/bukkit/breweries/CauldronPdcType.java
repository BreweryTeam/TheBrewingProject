package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class CauldronPdcType implements PersistentDataType<String, CauldronType> {

    public static final CauldronPdcType INSTANCE = new CauldronPdcType();

    @NotNull
    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @NotNull
    @Override
    public Class<CauldronType> getComplexType() {
        return CauldronType.class;
    }

    @NotNull
    @Override
    public String toPrimitive(@NotNull CauldronType complex, @NotNull PersistentDataAdapterContext context) {
        return complex.key().toString();
    }

    @NotNull
    @Override
    public CauldronType fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return BreweryRegistry.CAULDRON_TYPE.get(BreweryKey.parse(primitive));
    }
}
