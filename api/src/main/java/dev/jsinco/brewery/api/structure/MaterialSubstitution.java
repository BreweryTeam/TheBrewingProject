package dev.jsinco.brewery.api.structure;

import dev.jsinco.brewery.api.util.Holder;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record MaterialSubstitution(Holder.Material from, Set<Holder.Material> to) {

    public MaterialSubstitution(String from, String... to) {
        this(Holder.Material.fromMinecraftId(from), Arrays.stream(to)
                .map(Holder.Material::fromMinecraftId)
                .collect(Collectors.toSet())
        );
    }
}
