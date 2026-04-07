package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.bukkit.brew.BrewingStepPdcType;
import dev.jsinco.brewery.util.DecoderEncoder;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ListPersistentDataType<T> implements PersistentDataType<byte[], List<T>> {

    private final PersistentDataType<byte[], T> persistentDataType;

    public static final ListPersistentDataType<String> STRING_LIST = new ListPersistentDataType<>(new StringPdcType());
    public static final ListPersistentDataType<BrewingStep> BREWING_STEP_CIPHERED_LIST = new ListPersistentDataType<>(new BrewingStepPdcType(true));
    public static final ListPersistentDataType<BrewingStep> BREWING_STEP_LIST = new ListPersistentDataType<>(new BrewingStepPdcType(false));

    public ListPersistentDataType(PersistentDataType<byte[], T> persistentDataType) {
        this.persistentDataType = persistentDataType;
    }

    @NonNull
    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NonNull
    @Override
    public Class<List<T>> getComplexType() {
        return (Class<List<T>>) List.of().getClass();
    }

    @NonNull
    @Override
    public byte[] toPrimitive(@NonNull List<T> complex, @NonNull PersistentDataAdapterContext context) {
        byte[][] byteArrayArray = complex.stream()
                .map(complexElement -> persistentDataType.toPrimitive(complexElement, context))
                .toArray(byte[][]::new);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (DataOutputStream outputStream = new DataOutputStream(output)) {
            DecoderEncoder.encode(byteArrayArray, outputStream);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull List<T> fromPrimitive(@NonNull byte[] primitive, @NonNull PersistentDataAdapterContext context) {
        try {
            byte[][] byteArrayArray = DecoderEncoder.decode(new ByteArrayInputStream(primitive));
            return Arrays.stream(byteArrayArray)
                    .map(primitiveElement -> persistentDataType.fromPrimitive(primitiveElement, context))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
