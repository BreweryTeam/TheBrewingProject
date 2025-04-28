package dev.jsinco.brewery.util;

import java.util.List;
import java.util.Random;

public class RandomUtil {
    private static final Random RANDOM = new Random();

    public static <T extends WeightedProbabilityElement> T randomWeighted(List<T> tList) {
        int[] cumulativeSums = new int[tList.size()];
        int cumulativeSum = 0;
        for (int i = 0; i < tList.size(); i++) {
            T drunkEvent = tList.get(i);
            cumulativeSum += drunkEvent.probabilityWeight();
            cumulativeSums[i] = cumulativeSum;
        }
        int randomInt = RANDOM.nextInt(cumulativeSum);
        for (int i = 0; i < tList.size(); i++) {
            if (cumulativeSums[i] > randomInt) {
                return tList.get(i);
            }
        }
        return tList.getLast();
    }

    public static int cumulativeSum(List<? extends WeightedProbabilityElement> tList) {
        int cumulativeSum = 0;
        for (int i = 0; i < tList.size(); i++) {
            WeightedProbabilityElement drunkEvent = tList.get(i);
            cumulativeSum += drunkEvent.probabilityWeight();
        }
        return cumulativeSum;
    }

}
