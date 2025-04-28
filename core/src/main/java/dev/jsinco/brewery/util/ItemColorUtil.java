package dev.jsinco.brewery.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Map;

public class ItemColorUtil {

    private static Map<String, Color> itemColors = compileItemColors();

    private ItemColorUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static Map<String, Color> compileItemColors() {
        JsonObject jsonObject = FileUtil.readJsonResource("/colors.json").getAsJsonObject();
        ImmutableMap.Builder<String, Color> immutableMapBuilder = ImmutableMap.builder();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            immutableMapBuilder.put("minecraft:" + entry.getKey(), new Color(Integer.parseInt(entry.getValue().getAsString(), 16)));
        }
        return immutableMapBuilder.build();
    }

    public static @Nullable Color getItemColor(String itemId) {
        return itemColors.get(itemId);
    }
}
