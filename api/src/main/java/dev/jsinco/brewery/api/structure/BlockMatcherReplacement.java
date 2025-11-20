package dev.jsinco.brewery.api.structure;

import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.api.util.Materials;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public record BlockMatcherReplacement(Set<Materials> alternativesBacking, Holder.Material original) {

    public Set<Holder.Material> alternatives() {
        return alternativesBacking.stream()
                .map(Materials::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public record List(java.util.List<BlockMatcherReplacement> elements) {
        public List() {
            this(java.util.List.of());
        }

        public List {
            elements = java.util.List.copyOf(elements);
        }
    }
}
