package dev.jsinco.brewery.bukkit.structure.serializer;

import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.api.util.Materials;
import eu.okaeri.configs.exception.OkaeriConfigException;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class MaterialsSerializer implements ObjectSerializer<Materials> {
    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Materials.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull Materials object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        switch (object) {
            case Materials.TagBacked tagBacked ->
                    data.setValue("#" + tagBacked.key().minimalized(Key.MINECRAFT_NAMESPACE));
            case Materials.Singleton singleton -> data.setValue(singleton.backing());
            case Materials.SetBacked setBacked -> data.setValueCollection(setBacked.values(), Holder.Material.class);
        }
    }

    @Override
    public Materials deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        if (isList(data)) {
            return new Materials.SetBacked(data.getValueAsList(Materials.class)
                    .stream()
                    .map(Materials::values)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet())
            );
        }
        String value = data.getValue(String.class);
        if (value.startsWith("#")) {
            return new Materials.TagBacked(BreweryKey.parse(value.replaceFirst("^#", ""), Key.MINECRAFT_NAMESPACE));
        }
        return new Materials.Singleton(data.getValue(Holder.Material.class));
    }

    private boolean isList(DeserializationData data) {
        try {
            data.getValueAsList(String.class);
            return true;
        } catch (IllegalArgumentException | OkaeriConfigException e) {
            return false;
        }
    }
}
