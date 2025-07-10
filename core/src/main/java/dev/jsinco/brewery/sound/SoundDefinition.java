package dev.jsinco.brewery.sound;

import dev.jsinco.brewery.math.RangeF;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;

public record SoundDefinition(Sound.Builder sound, RangeF pitch) {

    /**
     * Builds and returns the sound with processed values (e.g. random pitch)
     *
     * @return The sound
     */
    @Override
    public Sound.Builder sound() {
        return sound.pitch(pitch.getRandom());
    }
}
