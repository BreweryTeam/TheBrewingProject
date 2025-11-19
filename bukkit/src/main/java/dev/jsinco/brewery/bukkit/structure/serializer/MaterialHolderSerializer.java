package dev.jsinco.brewery.bukkit.structure.serializer;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

public class MaterialHolderSerializer implements ObjectSerializer<Holder.Material> {
    @Override
    public boolean supports(@NonNull Class<? super Holder.Material> type) {
        return Holder.Material.class == type;
    }

    @Override
    public void serialize(Holder.@NonNull Material object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(object.value().minimalized(Key.MINECRAFT_NAMESPACE));
    }

    @Override
    public Holder.Material deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String value = data.getValue(String.class);
        NamespacedKey key = NamespacedKey.fromString(value);
        Preconditions.checkArgument(key != null, "Invalid key: " + value);
        Material material = Registry.MATERIAL.get(key);
        Preconditions.checkArgument(material != null, "Unknown material with key: " + key);
        return BukkitAdapter.toMaterialHolder(material);
    }
}
