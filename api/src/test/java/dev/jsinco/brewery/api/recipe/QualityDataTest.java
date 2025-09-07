package dev.jsinco.brewery.api.recipe;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class QualityDataTest {

    @ParameterizedTest
    @MethodSource("testData")
    void readQualityFactoredString(String testData) {
        assertDoesNotThrow(() -> QualityData.readQualityFactoredString(testData));
    }

    private static Stream<Arguments> testData() {
        return Stream.of(
                "Rancid Vodka/<gold>Golden Vodka/</gold><gold>Shimmering Golden Vodka",
                "/Rancid Vodka/<gold>Golden Vodka</gold><gold>Shimmering Golden Vodka",
                "Rancid Vodka/<gold>Golden Vodka</gold><gold>Shimmering Golden Vodka/",
                "//"
        ).map(Arguments::of);
    }
}