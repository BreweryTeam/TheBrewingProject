package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.World;

public final class SoundPlayer {
    public static void playSoundEffect(String sound, Sound.Source source, Location location) {
        Sound.Builder builder = TheBrewingProject.getInstance().getSoundManager().getSound(sound);
        if (builder == null) {
            return;
        }

        builder.source(source);
        location.getWorld().playSound(builder.build(), location.x(), location.y(), location.z());
    }

    public static void playSoundEffect(String sound, Sound.Source source, World world, double x, double y, double z) {
        playSoundEffect(sound, source, new Location(world, x, y, z));
    }
}
