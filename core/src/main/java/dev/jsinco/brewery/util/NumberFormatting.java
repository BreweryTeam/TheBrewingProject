package dev.jsinco.brewery.util;

import java.util.List;

public class NumberFormatting {

    private static final List<Numeral> ROMAN_NUMERAL = List.of(
            new Numeral("M", 1000, true),
            new Numeral("D", 500, false),
            new Numeral("C", 100, true),
            new Numeral("L", 50, false),
            new Numeral("X", 10, true),
            new Numeral("V", 5, false),
            new Numeral("I", 1, true)
    );

    private NumberFormatting() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String toRoman(int number) {
        StringBuilder builder = new StringBuilder();
        int leftoverNumber = number;
        for (int i = 0; i < ROMAN_NUMERAL.size(); i++) {
            Numeral numeral = ROMAN_NUMERAL.get(i);
            if (numeral.canBeNegator()) {
                while (numeral.number() <= leftoverNumber) {
                    leftoverNumber -= numeral.number();
                    builder.append(numeral.character());
                }
                if (i + 1 >= ROMAN_NUMERAL.size()) {
                    continue;
                }
                Numeral negator = ROMAN_NUMERAL.get(i + 2);
                if (numeral.number() - negator.number() <= leftoverNumber) {
                    leftoverNumber -= numeral.number() - negator.number();
                    builder.append(negator.character()).append(numeral.character());
                }
            } else {
                Numeral negator = ROMAN_NUMERAL.get(i + 1);
                if (numeral.number() <= leftoverNumber) {
                    leftoverNumber -= numeral.number();
                    builder.append(numeral.character());
                } else if (numeral.number() - negator.number() <= leftoverNumber) {
                    leftoverNumber -= numeral.number() - negator.number();
                    builder.append(negator.character()).append(numeral.character());
                }
            }
        }
        return builder.toString();
    }

    private record Numeral(String character, int number, boolean canBeNegator) {

    }
}
