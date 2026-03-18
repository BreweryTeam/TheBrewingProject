package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

public class CauldronPdcType implements PersistentDataType<String, CauldronType> {

    public static final CauldronPdcType INSTANCE = new CauldronPdcType();

    @NonNull
    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @NonNull
    @Override
    public Class<CauldronType> getComplexType() {
        return CauldronType.class;
    }

    @NonNull
    @Override
    public String toPrimitive(@NonNull CauldronType complex, @NonNull PersistentDataAdapterContext context) {
        return complex.key().toString();
    }

    @NonNull
    @Override
    public CauldronType fromPrimitive(@NonNull String primitive, @NonNull PersistentDataAdapterContext context) {
        return BreweryRegistry.CAULDRON_TYPE.get(BreweryKey.parse(primitive));
    }
}
