package dev.jsinco.brewery.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.Map;

public class PresetColorsUtil {

    private static final Map<Key, Color> ITEM_COLORS = compileItemColors();
    private static final Map<Key, Color> BIOME_WATER_COLORS = compileWaterColors();


    private PresetColorsUtil() {
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

    private static Map<Key, Color> compileWaterColors() {
        JsonObject jsonObject = FileUtil.readJsonResource("/biomes.json").getAsJsonObject();
        ImmutableMap.Builder<Key, Color> immutableMapBuilder = ImmutableMap.builder();
        for (Map.Entry<String, JsonElement> entry : jsonObject.get("water_color").getAsJsonObject().entrySet()) {
            immutableMapBuilder.put(Key.key(entry.getKey()), new Color(Integer.parseInt(entry.getValue().getAsString(), 16)));
        }
        return immutableMapBuilder.build();
    }

    public static @Nullable Color getItemColor(Key itemId) {
        return ITEM_COLORS.get(itemId);
    }


    public static @Nullable Color getWaterColor(Key biomeId) {
        return BIOME_WATER_COLORS.get(biomeId);
    }
}
