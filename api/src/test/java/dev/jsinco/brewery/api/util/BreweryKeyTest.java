package dev.jsinco.brewery.api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BreweryKeyTest {

    @Test
    public void lowercaseInput() {
        BreweryKey bk = new BreweryKey("brewery", "key");
        assertEquals("brewery", bk.namespace());
        assertEquals("key", bk.key());
    }

    @Test
    public void namespaceIsLowercased() {
        BreweryKey bk = new BreweryKey("Brewery", "key");
        assertEquals("brewery", bk.namespace());
        assertEquals("key", bk.key());
    }

    @Test
    public void keyIsLowercased() {
        BreweryKey bk = new BreweryKey("brewery", "Key");
        assertEquals("brewery", bk.namespace());
        assertEquals("key", bk.key());
    }

    @Test
    public void mythicNamespaceIsCaseSensitive() {
        BreweryKey bk = new BreweryKey("mythic", "Key");
        assertEquals("mythic", bk.namespace());
        assertEquals("Key", bk.key());
    }

}