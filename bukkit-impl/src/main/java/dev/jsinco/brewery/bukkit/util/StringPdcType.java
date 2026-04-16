package dev.jsinco.brewery.bukkit.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;

public class StringPdcType implements PersistentDataType<byte[], String> {

    @NonNull
    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NonNull
    @Override
    public Class<String> getComplexType() {
        return String.class;
    }


    @NonNull
    @Override
    public byte[] toPrimitive(@NonNull String complex, @NonNull PersistentDataAdapterContext context) {
        return complex.getBytes(StandardCharsets.UTF_8);
    }

    @NonNull
    @Override
    public String fromPrimitive(@NonNull byte[] primitive, @NonNull PersistentDataAdapterContext context) {
        return new String(primitive, StandardCharsets.UTF_8);
    }
}
