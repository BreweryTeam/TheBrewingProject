package dev.jsinco.brewery.bukkit.meta;

import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class PdcMetaDataType implements MetaDataType<MetaData, PersistentDataContainer> {

    private final PersistentDataAdapterContext context;

    private PdcMetaDataType(PersistentDataAdapterContext context) {
        this.context = context;
    }

    public static PdcMetaDataType with(PersistentDataAdapterContext context) {
        return new PdcMetaDataType(context);
    }

    @Override
    public Class<MetaData> getPrimitiveType() {
        return MetaData.class;
    }

    @Override
    public Class<PersistentDataContainer> getComplexType() {
        return PersistentDataContainer.class;
    }

    @Override
    @SuppressWarnings("unchecked") // typeof check ensures safety
    public MetaData toPrimitive(PersistentDataContainer complex) {
        MetaData meta = new MetaData();
        for (NamespacedKey key : complex.getKeys()) {
            Object value = complex.get(key, MetaUtil.findType(complex, key));
            meta = meta.withMeta(Key.key(key.namespace(), key.value()), (MetaDataType<?, Object>) MetaUtil.metaDataTypeOf(value), value);
        }
        return meta;
    }

    @Override
    @SuppressWarnings("unchecked") // typeof check ensures safety
    public PersistentDataContainer toComplex(MetaData primitive) {
        PersistentDataContainer pdc = context.newPersistentDataContainer();
        for (Map.Entry<Key, Object> entry : primitive.primitiveMap().entrySet()) {
            NamespacedKey key = new NamespacedKey(entry.getKey().namespace(), entry.getKey().value());
            Object value = entry.getValue();
            pdc.set(key, (PersistentDataType<?, Object>) MetaUtil.pdcTypeOf(value), value);
        }
        return pdc;
    }

}
