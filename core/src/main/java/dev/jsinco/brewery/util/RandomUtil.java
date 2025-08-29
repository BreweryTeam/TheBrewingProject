package dev.jsinco.brewery.util;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class RandomUtil {
    private static final Random RANDOM = new Random();

    public static <T> T randomWeighted(List<T> tList, Function<T, Double> toWeight) {
        double[] cumulativeSums = new double[tList.size()];
        double cumulativeSum = 0;
        for (int i = 0; i < tList.size(); i++) {
            T t = tList.get(i);
            cumulativeSum += toWeight.apply(t);
            cumulativeSums[i] = cumulativeSum;
        }
        double randomInt = RANDOM.nextDouble(cumulativeSum);
        for (int i = 0; i < tList.size(); i++) {
            if (cumulativeSums[i] > randomInt) {
                return tList.get(i);
            }
        }
        return tList.getLast();
    }

}
