package dev.jsinco.brewery.meta;

import com.google.gson.JsonObject;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetaSerializerTest {

    @ParameterizedTest(name = "{index} ==> round trip with type {0} and value {1}")
    @MethodSource("dev.jsinco.brewery.api.meta.MetaDataSamples#metaDataSampleProvider")
    <C> void testRoundTrip(MetaDataType<?, C> type, C testValue) {
        Key testKey = Key.key("test", "write_and_read");
        MetaData meta = new MetaData().withMeta(testKey, type, testValue);

        JsonObject json = MetaSerializer.INSTANCE.serialize(meta);
        System.out.println(json);
        MetaData retrievedMeta = MetaSerializer.INSTANCE.deserialize(json);

        assertEquals(meta, retrievedMeta, "Retrieved meta should match the written meta");
    }

}
