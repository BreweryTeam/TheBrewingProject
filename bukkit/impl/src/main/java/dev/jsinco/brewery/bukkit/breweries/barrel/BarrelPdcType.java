package dev.jsinco.brewery.bukkit.breweries.barrel;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

public class BarrelPdcType implements PersistentDataType<String, BarrelType> {
    public static final PersistentDataType<String, BarrelType> INSTANCE = new BarrelPdcType();

    @NonNull
    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @NonNull
    @Override
    public Class<BarrelType> getComplexType() {
        return BarrelType.class;
    }

    @NonNull
    @Override
    public String toPrimitive(@NonNull BarrelType complex, @NonNull PersistentDataAdapterContext context) {
        return complex.key().toString();
    }

    @NonNull
    @Override
    public BarrelType fromPrimitive(@NonNull String primitive, @NonNull PersistentDataAdapterContext context) {
        return BreweryRegistry.BARREL_TYPE.get(BreweryKey.parse(primitive));
    }
}
