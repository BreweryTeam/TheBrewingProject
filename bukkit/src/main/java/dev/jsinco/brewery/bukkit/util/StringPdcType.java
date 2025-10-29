package dev.jsinco.brewery.bukkit.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class StringPdcType implements PersistentDataType<byte[], String> {

    @NotNull
    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NotNull
    @Override
    public Class<String> getComplexType() {
        return String.class;
    }


    @NotNull
    @Override
    public byte[] toPrimitive(@NotNull String complex, @NotNull PersistentDataAdapterContext context) {
        return complex.getBytes(StandardCharsets.UTF_8);
    }

    @NotNull
    @Override
    public String fromPrimitive(@NotNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {
        return new String(primitive, StandardCharsets.UTF_8);
    }
}
