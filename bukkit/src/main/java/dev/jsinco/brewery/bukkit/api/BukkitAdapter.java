package dev.jsinco.brewery.bukkit.api;

import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class BukkitAdapter {

    public static Optional<Location> toLocation(BreweryLocation location) {
        return Optional.ofNullable(Bukkit.getWorld(location.worldUuid()))
                .map(world -> new Location(world, location.x(), location.y(), location.z()));
    }

    public static BreweryLocation toBreweryLocation(Location location) {
        return new BreweryLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID());
    }

    public static BreweryLocation toBreweryLocation(Block block) {
        return new BreweryLocation(block.getX(), block.getY(), block.getZ(), block.getWorld().getUID());
    }

    public static Optional<Block> toBlock(BreweryLocation location) {
        return Optional.ofNullable(Bukkit.getWorld(location.worldUuid()))
                .map(world -> world.getBlockAt(location.x(), location.y(), location.z()));
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

    public static @Nullable Material toMaterial(Holder.Material material) {
        return Registry.MATERIAL.get(toNamespacedKey(material.value()));
    }

    public static Holder.@NonNull Material toMaterialHolder(Material material) {
        return new Holder.Material(toBreweryKey(material.getKey()));
    }

    public static Holder.Player toPlayerHolder(@NonNull Player player) {
        return new Holder.Player(player.getUniqueId());
    }

    public static Optional<Player> toPlayer(Holder.@NonNull Player player) {
        return Optional.ofNullable(Bukkit.getPlayer(player.value()));
    }

    public static Holder.World toWorldHolder(@NonNull World world) {
        return new Holder.World(world.getUID());
    }

    public static Optional<World> toWorldHolder(Holder.@NonNull World world) {
        return Optional.ofNullable(Bukkit.getWorld(world.value()));
    }
}
