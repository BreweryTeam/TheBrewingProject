package dev.jsinco.brewery.bukkit.breweries.barrel;

import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Registry;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class BarrelPdcType implements PersistentDataType<String, BarrelType> {
    public static final PersistentDataType<String, BarrelType> INSTANCE = new BarrelPdcType();

    @NotNull
    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @NotNull
    @Override
    public Class<BarrelType> getComplexType() {
        return BarrelType.class;
    }

    @NotNull
    @Override
    public String toPrimitive(@NotNull BarrelType complex, @NotNull PersistentDataAdapterContext context) {
        return complex.key().toString();
    }

    @NotNull
    @Override
    public BarrelType fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return Registry.BARREL_TYPE.get(BreweryKey.parse(primitive));
    }
}
