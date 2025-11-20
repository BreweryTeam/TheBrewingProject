package dev.jsinco.brewery.bukkit.structure.serializer;

import dev.jsinco.brewery.api.structure.BlockMatcherReplacement;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.api.util.Materials;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.Set;

public class BlockMatcherReplacementSerializer implements ObjectSerializer<BlockMatcherReplacement> {
    @Override
    public boolean supports(@NonNull Class<? super BlockMatcherReplacement> type) {
        return BlockMatcherReplacement.class == type;
    }

    @Override
    public void serialize(@NonNull BlockMatcherReplacement object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("original", object.original());
        data.addCollection("replacement", object.alternativesBacking(), Materials.class);
    }

    @Override
    public BlockMatcherReplacement deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        Holder.Material original = data.get("original", Holder.Material.class);
        if (original == null) {
            return null;
        }
        if (data.get("replacement", String.class) != null) {
            Materials replacement = data.get("replacement", Materials.class);
            return new BlockMatcherReplacement(Set.of(replacement), original);
        } else {
            Set<Materials> replacement = data.getAsSet("replacement", Materials.class);
            return new BlockMatcherReplacement(
                    replacement,
                    original
            );
        }
    }
}
