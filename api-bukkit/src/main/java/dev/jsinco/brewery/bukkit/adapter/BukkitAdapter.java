package dev.jsinco.brewery.bukkit.adapter;

import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Holder;
import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static @NotNull Holder.Material toMaterialHolder(Material material) {
        return new Holder.Material(toBreweryKey(material.getKey()));
    }

    public static Holder.Player toPlayerHolder(@NotNull Player player) {
        return new Holder.Player(player.getUniqueId());
    }

    public static Optional<Player> toPlayer(@NotNull Holder.Player player) {
        return Optional.ofNullable(Bukkit.getPlayer(player.value()));
    }

    public static Holder.World toWorldHolder(@NotNull World world) {
        return new Holder.World(world.getUID());
    }

    public static Optional<World> toWorldHolder(@NotNull Holder.World world) {
        return Optional.ofNullable(Bukkit.getWorld(world.value()));
    }
}
