package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.UUID;

public class BreweryLocationSerializer implements TypeSerializer<BreweryLocation> {
    @Override
    public BreweryLocation deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String string = node.getString();
        if (string == null) {
            throw new SerializationException("Can not deserialize empty node");
        }
        String[] split = Arrays.stream(string.split(",")).map(String::trim).toArray(String[]::new);
        if (split.length != 4) {
            throw new SerializationException("Expected location of format world, x, y, z");
        }
        World world;
        try {
            world = Bukkit.getWorld(UUID.fromString(split[0]));
        } catch (IllegalArgumentException e) {
            world = Bukkit.getWorld(split[0]);
        }
        if (world == null) {
            throw new SerializationException("Could not find world: " + split[0]);
        }
        try {
            int x = Integer.parseInt(split[1]);
            int y = Integer.parseInt(split[2]);
            int z = Integer.parseInt(split[3]);
            return new BreweryLocation(x, y, z, world.getUID());
            return new BreweryLocation(x, y, z, world.getUID());
        } catch (IllegalArgumentException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public void serialize(Type type, @Nullable BreweryLocation obj, ConfigurationNode node) throws SerializationException {
        node.set(
                String.format("%s, %d, %d, %d", Bukkit.getWorld(obj.worldUuid()).getName(), obj.x(), obj.y(), obj.z())
        );
    }
}
