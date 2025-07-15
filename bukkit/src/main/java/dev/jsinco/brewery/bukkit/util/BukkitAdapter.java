package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Holder;
import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BukkitAdapter {

    public static Location toLocation(BreweryLocation location) {
        return new Location(Bukkit.getWorld(location.worldUuid()), location.x(), location.y(), location.z());
    }

    public static BreweryLocation toBreweryLocation(Location location) {
        return new BreweryLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID());
    }

    public static BreweryLocation toBreweryLocation(Block block) {
        return new BreweryLocation(block.getX(), block.getY(), block.getZ(), block.getWorld().getUID());
    }

    public static Block toBlock(BreweryLocation location) {
        return Bukkit.getWorld(location.worldUuid()).getBlockAt(location.x(), location.y(), location.z());
    }

    public static NamespacedKey toNamespacedKey(BreweryKey breweryKey) {
        return NamespacedKey.fromString(breweryKey.toString());
    }

    public static BreweryKey toBreweryKey(NamespacedKey namespacedKey) {
        return new BreweryKey(namespacedKey.namespace(), namespacedKey.getKey());
    }

    public static Optional<World> toWorld(BreweryLocation location) {
        return Optional.ofNullable(Bukkit.getWorld(location.worldUuid()));
    }

    public static @Nullable Location parseLocation(String teleport) {
        String[] split = teleport.split(",");
        World world = Bukkit.getWorld(split[0]);
        if (world == null) {
            return null;
        }
        int x = Integer.parseInt(split[1].strip());
        int y = Integer.parseInt(split[2].strip());
        int z = Integer.parseInt(split[3].strip());
        return new Location(world, x, y, z);
    }

    public static @Nullable Material toMaterial(Holder.Material material) {
        return Registry.MATERIAL.get(toNamespacedKey(material.value()));
    }

    public static @NotNull Holder.Material toMaterialHolder(Material material) {
        return new Holder.Material(toBreweryKey(material.getKey()));
    }
}
