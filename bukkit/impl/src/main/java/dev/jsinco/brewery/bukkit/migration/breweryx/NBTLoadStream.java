package dev.jsinco.brewery.bukkit.migration.breweryx;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayInputStream;

public class NBTLoadStream extends ByteArrayInputStream {

    private static final String TAG = "brewdata";
    private static final NamespacedKey KEY = new NamespacedKey("breweryx", TAG);
    private static final NamespacedKey LEGACY_KEY = new NamespacedKey("brewery", TAG.toLowerCase());

    public NBTLoadStream(ItemMeta meta) {
        super(getNBTBytes(meta));
    }

    private static byte[] getNBTBytes(ItemMeta meta) {
        byte[] bytes = meta.getPersistentDataContainer().get(KEY, org.bukkit.persistence.PersistentDataType.BYTE_ARRAY);
        if (bytes == null) {
            bytes = meta.getPersistentDataContainer().get(LEGACY_KEY, org.bukkit.persistence.PersistentDataType.BYTE_ARRAY);
        }
        if (bytes == null) {
            return new byte[0];
        }
        return bytes;
    }

    public boolean hasData() {
        return count > 0;
    }
}