package dev.jsinco.brewery.configuration.structure;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public sealed interface BlockReplacement permits GenericMatcherDefinition, TypedMatcherDefinition {

    List<String> validate(Set<String> allowed);
}
