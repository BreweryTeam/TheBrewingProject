package dev.jsinco.brewery.util;

import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FutureUtilTest {

    @RepeatedTest(1000)
    void mergeFutures() {
        List<Integer> expected = new ArrayList<>();
        List<CompletableFuture<Integer>> actualFuture = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            expected.add(i);
            final int iFinal = i;
            actualFuture.add(CompletableFuture.supplyAsync(() -> iFinal));
        }
        assertEquals(expected, FutureUtil.mergeFutures(actualFuture).join());
    }
}