package dev.jsinco.brewery.sound;

import dev.jsinco.brewery.math.RangeF;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public record SoundDefinition(Key soundKey, RangeF pitch) {

    /**
     * Keys are not validated, this is a cheat to avoid having to do null checks
     */
    public static final SoundDefinition SILENT = new SoundDefinition(Key.key("brewery:silent"), new RangeF(0f, 0f));

    /**
     * Builds and returns the sound with processed values (e.g. random pitch)
     *
     * @return The sound
     */
    public Sound.Builder sound() {
        return Sound.sound().type(soundKey).pitch(pitch.getRandom());
    }
}
