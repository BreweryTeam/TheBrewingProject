package dev.jsinco.brewery.util;

import java.util.List;
import java.util.Random;

public final class Util {

    private static final Random RANDOM = new Random();

    public static int getIntDefaultZero(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static <T> T getRandomElement(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}
