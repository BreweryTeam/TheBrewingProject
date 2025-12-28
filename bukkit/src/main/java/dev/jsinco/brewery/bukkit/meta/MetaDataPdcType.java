package dev.jsinco.brewery.bukkit.meta;

import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Objects;

public class MetaDataPdcType implements PersistentDataType<PersistentDataContainer, MetaData> {

    public static final MetaDataPdcType INSTANCE = new MetaDataPdcType();
    public static final ListPersistentDataType<PersistentDataContainer, MetaData> LIST = PersistentDataType.LIST.listTypeFrom(INSTANCE);

    @NotNull
    @Override
    public Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @NotNull
    @Override
    public Class<MetaData> getComplexType() {
        return MetaData.class;
    }

    @Override
    @SuppressWarnings("unchecked") // typeof check ensures safety
    public @NonNull PersistentDataContainer toPrimitive(@NonNull MetaData complex, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer pdc = context.newPersistentDataContainer();
        for (Map.Entry<Key, Object> entry : complex.primitiveMap().entrySet()) {
            NamespacedKey key = new NamespacedKey(entry.getKey().namespace(), entry.getKey().value());
            Object value = entry.getValue();
            pdc.set(key, (PersistentDataType<?, Object>) MetaUtil.pdcTypeOf(value), value);
        }
        return pdc;
    }

    @Override
    @SuppressWarnings("unchecked") // typeof check ensures safety
    public @NonNull MetaData fromPrimitive(@NonNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
        MetaData meta = new MetaData();
        for (NamespacedKey key : primitive.getKeys()) {
            Object value = Objects.requireNonNull(primitive.get(key, MetaUtil.findType(primitive, key)));
            meta = meta.withMeta(Key.key(key.namespace(), key.value()), (MetaDataType<?, Object>) MetaUtil.metaDataTypeOf(value), value);
        }
        return meta;
    }

}
