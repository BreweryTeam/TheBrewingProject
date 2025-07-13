package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.util.Holder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class MaterialSerializer implements TypeSerializer<Holder.Material> {
    @Override
    public Holder.Material deserialize(@NotNull Type type, ConfigurationNode node) throws SerializationException {
        String value = node.getString();
        if (value == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.fromString(value);
        if (key == null) {
            throw new SerializationException("Invalid key: " + value);
        }
        Material material = Registry.MATERIAL.get(key);
        if (material == null) {
            throw new SerializationException("Unknown material key: " + value);
        }
        return new Holder.Material(BukkitAdapter.toBreweryKey(key));
    }

    @Override
    public void serialize(@NotNull Type type, Holder.@Nullable Material obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(obj.value().toString());
    }
}
