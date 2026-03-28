package dev.jsinco.brewery.configuration.structure;

import java.util.List;
import java.util.Set;

public sealed interface BlockReplacement permits GenericBlockReplacement, KeyedBlockReplacement {

    List<String> validate(Set<String> allowed);
}
