package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.structure.SinglePositionStructure;
import org.jetbrains.annotations.NotNull;

public interface Cauldron extends Tickable, SinglePositionStructure {

    long getTime();

    boolean isHot();

    @NotNull Brew getBrew();
}
