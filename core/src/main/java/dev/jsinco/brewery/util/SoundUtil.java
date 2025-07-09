package dev.jsinco.brewery.util;


import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.math.RangeF;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SoundUtil {
    private static Sound.Builder parseSoundDefinition(String soundDefinition) {
        String[] split = soundDefinition.split("/");

        if  (split.length == 0 || split.length > 2) {
            throw new IllegalArgumentException("Invalid sound definition");
        }

        Sound.Builder builder = Sound.sound()
            .type(Key.key(split[0]));

        if (split.length > 1)
            builder.pitch(new RangeF(split[1]).getRandom());

        return builder;
    }

    public static @Nullable Sound.Builder selectSound(String id) {
        Object configValue = Config.SOUNDS.get(id);

        switch (configValue) {
            case null -> {
                return null;
            }
            case String configValueString -> {
                return parseSoundDefinition(configValueString);
            }
            case List<?> configValueList -> {
                List<String> soundIds = configValueList.stream().map(element -> (String) element).toList();

                return parseSoundDefinition(Util.getRandomElement(soundIds));
            }
            default -> throw new IllegalArgumentException("Invalid sound definition");
        }
    }
}
