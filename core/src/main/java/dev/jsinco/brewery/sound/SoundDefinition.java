package dev.jsinco.brewery.sound;

import dev.jsinco.brewery.api.math.RangeF;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.util.List;
import java.util.Random;

public record SoundDefinition(List<SoundSetting> sounds) {

    private static final Random RANDOM = new Random();

    /**
     * Builds and returns the sound with processed values (e.g. random pitch)
     *
     * @return The sound
     */
    public Sound.Builder sound() {
        SoundSetting soundSetting = sounds.get(RANDOM.nextInt(sounds.size()));
        return Sound.sound().type(soundSetting.soundKey()).pitch(soundSetting.pitch().getRandom());
    }

    public record SoundSetting(Key soundKey, RangeF pitch) {

    }
}
