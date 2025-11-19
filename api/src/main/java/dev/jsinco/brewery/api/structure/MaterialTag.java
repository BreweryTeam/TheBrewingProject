package dev.jsinco.brewery.api.structure;

import dev.jsinco.brewery.api.util.Holder;

import java.util.Set;

public record MaterialTag(Set<Holder.Material> materials, int xRegion, int yRegion, int zRegion) {
}
