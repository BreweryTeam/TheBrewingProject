package dev.jsinco.brewery.bukkit.structure.serializer;

import dev.jsinco.brewery.api.structure.BlockMatcherReplacement;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public class BlockMatcherReplacementsSerializer implements ObjectSerializer<BlockMatcherReplacement.List> {
    @Override
    public boolean supports(@NonNull Class<? super BlockMatcherReplacement.List> type) {
        return BlockMatcherReplacement.List.class == type;
    }

    @Override
    public void serialize(@NotNull BlockMatcherReplacement.List object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValueCollection(object.elements(), BlockMatcherReplacement.class);
    }

    @Override
    public BlockMatcherReplacement.List deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        return new BlockMatcherReplacement.List(data.getValueAsList(BlockMatcherReplacement.class));
    }
}
