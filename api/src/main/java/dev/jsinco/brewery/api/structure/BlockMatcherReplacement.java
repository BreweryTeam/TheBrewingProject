package dev.jsinco.brewery.api.structure;

import dev.jsinco.brewery.api.util.Holder;

import java.util.Set;

public record BlockMatcherReplacement(Set<Holder.Material> alternatives, Holder.Material original) {


    public record List(java.util.List<BlockMatcherReplacement> elements) {
        public List() {
            this(java.util.List.of());
        }

        public List {
            elements = java.util.List.copyOf(elements);
        }
    }
}
