package dev.jsinco.brewery.api.meta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ListMetaDataTypeTest {

    @Test
    void testDoesAllowNotTooDeeplyNested() {
        MetaDataType<?, ?> type = MetaDataType.INTEGER;
        for (int i = 0; i < ListMetaDataType.MAX_DEPTH; i++) {
            type = ListMetaDataType.from(type);
        }
        MetaDataType<?, ?> finalType = type;
        assertDoesNotThrow(() -> ListMetaDataType.from(finalType));
    }

    @Test
    void testDoesNotAllowTooDeeplyNested() {
        MetaDataType<?, ?> type = MetaDataType.INTEGER;
        for (int i = 0; i < ListMetaDataType.MAX_DEPTH + 1; i++) {
            type = ListMetaDataType.from(type);
        }
        MetaDataType<?, ?> finalType = type;
        assertThrows(IllegalArgumentException.class, () -> ListMetaDataType.from(finalType));
    }

}
