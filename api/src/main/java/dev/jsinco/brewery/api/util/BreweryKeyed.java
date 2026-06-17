package dev.jsinco.brewery.api.util;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface BreweryKeyed {

    /**
     * @return The linked key to this object
     */
    BreweryKey key();
}
