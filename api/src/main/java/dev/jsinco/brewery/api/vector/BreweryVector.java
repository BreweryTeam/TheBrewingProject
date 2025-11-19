package dev.jsinco.brewery.api.vector;

import com.google.common.base.Preconditions;

/**
 * @param x Position
 * @param y Position
 * @param z Position
 */
public record BreweryVector(int x, int y, int z) {

    /**
     * Serialization friendly list instance
     */
    public record List(java.util.List<BreweryVector> elements) {


        public List {
            Preconditions.checkNotNull(elements);
        }
    }
}
