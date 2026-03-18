package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.sound.SoundDefinition;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class SoundPlayer {
    public static void playSoundEffect(@Nullable SoundDefinition sound, Sound.@NonNull Source source, @NonNull Location location) {
        if (sound == null) {
            return;
        }
        Sound.Builder builder = sound.sound();
        builder.source(source);
        location.getWorld().playSound(builder.build(), location.x(), location.y(), location.z());
    }

    public static void playSoundEffect(@Nullable SoundDefinition sound, Sound.@NonNull Source source, @NonNull World world, double x, double y, double z) {
        playSoundEffect(sound, source, new Location(world, x, y, z));
    }
}
