package dev.jsinco.brewery.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.Map;

public class ItemColorUtil {

    private static final Map<Key, Color> ITEM_COLORS = compileItemColors();

    private ItemColorUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static Map<Key, Color> compileItemColors() {
        JsonObject jsonObject = FileUtil.readJsonResource("/colors.json").getAsJsonObject();
        ImmutableMap.Builder<Key, Color> immutableMapBuilder = ImmutableMap.builder();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            immutableMapBuilder.put(Key.key(entry.getKey()), new Color(Integer.parseInt(entry.getValue().getAsString(), 16)));
        }
        return immutableMapBuilder.build();
    }

    public static @Nullable Color getItemColor(Key itemId) {
        return ITEM_COLORS.get(itemId);
    }
}
