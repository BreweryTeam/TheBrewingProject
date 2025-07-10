package dev.jsinco.brewery.sound;

import dev.jsinco.brewery.math.RangeF;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;

import java.util.Random;

@Getter
public class SoundDefinition {
    private final Sound.Builder sound;
    private final RangeF pitch;

    public SoundDefinition(Sound.Builder sound, RangeF pitch) {
        this.sound = sound;
        this.pitch = pitch;
    }

    /**
     * Builds and returns the sound with processed values (e.g. random pitch)
     *
     * @returns The sound
     */
    public Sound.Builder getSound() {
        return sound.pitch(pitch.getRandom());
    }
}
