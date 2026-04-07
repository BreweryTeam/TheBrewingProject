package dev.jsinco.brewery.bukkit.ingredient;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WildcardIngredientImplTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/ingredient/wildcard_mismatches.csv", delimiter = ' ')
    void wildcardMismatchTest(String wildcardArgument, String key) {
        assertFalse(WildcardIngredientImpl.matches(wildcardArgument.split(":"), key.split(":")));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/ingredient/wildcard_matches.csv", delimiter = ' ')
    void wildcardMatchTest(String wildcardArgument, String key) {
        assertTrue(WildcardIngredientImpl.matches(wildcardArgument.split(":"), key.split(":")));
    }
}