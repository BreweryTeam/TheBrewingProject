package dev.jsinco.brewery.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberFormattingTest {

    @ParameterizedTest
    @MethodSource("numeralArguments")
    void toRoman(String numerals, int inputValue) {
        assertEquals(numerals, NumberFormatting.toRoman(inputValue));
    }

    private static Stream<Arguments> numeralArguments() {
        return Stream.of(
                Arguments.of("XXXIX", 39),
                Arguments.of("CCXLVI", 246),
                Arguments.of("DCCLXXXIX", 789),
                Arguments.of("MIMICDXXI", 2421),
                Arguments.of("CLX", 160),
                Arguments.of("CCVII", 207),
                Arguments.of("MIIX", 1009),
                Arguments.of("MILXVI", 1066),
                Arguments.of("MIDCCLXXVI", 1776),
                Arguments.of("MICMIXVIII", 1918),
                Arguments.of("MICMIXLIV", 1944),
                Arguments.of("MIMIXXV", 2025)
        );
    }

}