package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

public class BreweryLocationSerializer implements TypeSerializer<Supplier<BreweryLocation>> {
    @Override
    public Supplier<BreweryLocation> deserialize(@NotNull Type type, ConfigurationNode node) throws SerializationException {
        String string = node.getString();
        if (string == null) {
            throw new SerializationException("Can not deserialize empty node");
        }
        String[] split = Arrays.stream(string.split(",")).map(String::trim).toArray(String[]::new);
        if (split.length != 4) {
            throw new SerializationException("Expected location of format world, x, y, z");
        }
        try {
            int x = Integer.parseInt(split[1]);
            int y = Integer.parseInt(split[2]);
            int z = Integer.parseInt(split[3]);
            return () -> {
                UUID worldUuid = parseWorld(split[0]);
                return new BreweryLocation(x, y, z, worldUuid);
            };
        } catch (IllegalArgumentException e) {
            throw new SerializationException(e);
        }

    }

    private UUID parseWorld(String worldString) {
        World world;
        try {
            world = Bukkit.getWorld(UUID.fromString(worldString));
        } catch (IllegalArgumentException e) {
            world = Bukkit.getWorld(worldString);
        }
        if (world == null) {
            throw new IllegalArgumentException("Could not find world: " + worldString);
        }
        return world.getUID();
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable Supplier<BreweryLocation> obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        BreweryLocation location = obj.get();
        node.set(
                String.format("%s, %d, %d, %d", Bukkit.getWorld(location.worldUuid()).getName(), location.x(), location.y(), location.z())
        );
    }
}
