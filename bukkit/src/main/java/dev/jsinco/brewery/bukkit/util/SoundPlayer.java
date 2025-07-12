package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.sound.SoundDefinition;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SoundPlayer {
    public static void playSoundEffect(@Nullable SoundDefinition sound, @NotNull Sound.Source source, @NotNull Location location) {
        if (sound == null) {
            return;
        }
        Sound.Builder builder = sound.sound();
        builder.source(source);
        location.getWorld().playSound(builder.build(), location.x(), location.y(), location.z());
    }

    public static void playSoundEffect(@Nullable SoundDefinition sound, @NotNull Sound.Source source, @NotNull World world, double x, double y, double z) {
        playSoundEffect(sound, source, new Location(world, x, y, z));
    }
}
