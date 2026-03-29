package dev.jsinco.brewery.configuration.structure;

import eu.okaeri.configs.serdes.SerializationData;

import java.util.List;
import java.util.Set;

public sealed interface BlockReplacement permits GenericBlockReplacement, KeyedBlockReplacement {

    List<String> validate(Set<String> allowed);

    void write(SerializationData data);
}
