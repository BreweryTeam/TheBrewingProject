package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.structure.SinglePositionStructure;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Cauldron extends Tickable, SinglePositionStructure {

    /**
     * @return The brew time in ticks
     */
    long getTime();

    /**
     * @return True if there's a heat source under the cauldron
     */
    boolean isHot();

    /**
     * @return The brew that is being made
     */
    @NotNull Brew getBrew();

    /**
     * The return value is optional, as this state is fetched from the world directly. Requires chunk loading
     *
     * @return The type of the cauldron
     */
    Optional<CauldronType> getType();

    /**
     * Requires chunk loading, checks the cauldron block for information
     *
     * @return How many brews can be extracted from this cauldron
     */
    int getLevel();
}
