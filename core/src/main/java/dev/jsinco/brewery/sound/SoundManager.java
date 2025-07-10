package dev.jsinco.brewery.sound;

import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.math.RangeF;
import dev.jsinco.brewery.util.Util;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SoundManager {
    /**
     * <p>
     * The sounds are stored as suppliers so we can easily customize how sounds get returned
     * </p>
     * <p>
     * For example, random sound from an array as shown below
     * </p>
     */
    private final Map<String, Supplier<SoundDefinition>> sounds = new HashMap<>();

    protected SoundDefinition parseSoundDefinition(String soundDefinition) {
        String[] split = soundDefinition.split("/");

        if  (split.length == 0 || split.length > 2) {
            throw new IllegalArgumentException("Invalid sound definition");
        }

        Sound.Builder sound = Sound.sound().type(Key.key(split[0]));
        RangeF pitchRange = split.length > 1 ? RangeF.fromString(split[1]) : new RangeF(1.0f, 1.0f);

        return new SoundDefinition(sound, pitchRange);
    }

    protected @Nullable Supplier<SoundDefinition> processConfigValue(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case String configValueString -> {
                SoundDefinition soundDefinition = parseSoundDefinition(configValueString);
                return () -> soundDefinition;
            }
            case List<?> configValueList -> {
                List<SoundDefinition> sounds = configValueList.stream()
                        .map(String.class::cast)
                        .map(this::parseSoundDefinition)
                        .toList();

                return () -> Util.getRandomElement(sounds);
            }
            default -> throw new IllegalArgumentException("Invalid sound definition");
        }
    }

    public @Nullable Sound.Builder getSound(String id) {
        Supplier<SoundDefinition> supplier = sounds.get(id);
        if (supplier == null) {
            return null;
        }

        return supplier.get().sound();
    }

    public void reload() {
        sounds.clear();
        Map<String, Object> configSounds = Config.SOUNDS;

        for (Map.Entry<String, Object> entry : configSounds.entrySet()) {
            Supplier<SoundDefinition> supplier = processConfigValue(entry.getValue());
            sounds.put(entry.getKey(), supplier);
        }
    }
}
