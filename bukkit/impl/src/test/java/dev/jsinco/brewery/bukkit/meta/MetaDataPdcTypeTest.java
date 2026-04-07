package dev.jsinco.brewery.bukkit.meta;

import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.persistence.PersistentDataContainerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockBukkitExtension.class)
public class MetaDataPdcTypeTest {

    @ParameterizedTest(name = "{index} ==> write and read with type {0} and value {1}")
    @MethodSource("dev.jsinco.brewery.api.meta.MetaDataSamples#metaDataSampleProvider")
    <C> void testWriteAndRead(MetaDataType<?, C> type, C testValue) {
        PersistentDataContainer pdc = new PersistentDataContainerMock();
        MetaData meta = new MetaData()
                .withMeta(Key.key("test", "write_and_read_pdc"), type, testValue);
        NamespacedKey key = new NamespacedKey("test", "meta");

        pdc.set(key, MetaDataPdcType.INSTANCE, meta);
        MetaData retrievedMeta = pdc.get(key, MetaDataPdcType.INSTANCE);
        assertEquals(meta, retrievedMeta);
    }

    @Test
    void testListTypeIsNotErased() {
        PersistentDataContainer pdc = new PersistentDataContainerMock();
        Key innerKey = Key.key("test", "list_erasure");
        MetaData meta = new MetaData()
                .withMeta(innerKey, MetaDataType.STRING_LIST, List.of("value"));
        NamespacedKey key = new NamespacedKey("test", "meta");

        pdc.set(key, MetaDataPdcType.INSTANCE, meta);
        MetaData retrievedMeta = pdc.get(key, MetaDataPdcType.INSTANCE);
        assertThrows(IllegalArgumentException.class, () -> retrievedMeta.meta(innerKey, MetaDataType.INTEGER_LIST));
    }

}
