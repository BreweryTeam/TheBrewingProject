package dev.jsinco.brewery.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LocationUtil {

    /**
     * Resolves an actual valid world UUID from a String that can be a UUID or the world name
     * @param worldNameOrUUID The world name or UUID
     * @return A valid World UUID
     */
    public static @NotNull UUID resolveWorld(String worldNameOrUUID) {
        World world;
        try {
            world = Bukkit.getWorld(UUID.fromString(worldNameOrUUID));
        } catch (IllegalArgumentException e) {
            world = Bukkit.getWorld(worldNameOrUUID);
        }
        if (world == null) {
            throw new IllegalArgumentException("Could not find world: " + worldNameOrUUID);
        }
        return world.getUID();
    }

}
