package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.util.Holder;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

public class MaterialSerializer implements ObjectSerializer<Holder.Material> {

    @Override
    public boolean supports(@NonNull Class<? super Holder.Material> type) {
        return Holder.Material.class == type;
    }

    @Override
    public void serialize(Holder.@NonNull Material object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(object.value().toString());
    }

    @Override
    public Holder.Material deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String value = data.getValue(String.class);
        NamespacedKey key = NamespacedKey.fromString(value);
        if (key == null) {
            throw new IllegalArgumentException("Invalid key: " + value);
        }
        Material material = Registry.MATERIAL.get(key);
        if (material == null) {
            throw new IllegalArgumentException("Unknown material key: " + value);
        }
        return new Holder.Material(BukkitAdapter.toBreweryKey(key));
    }
}
