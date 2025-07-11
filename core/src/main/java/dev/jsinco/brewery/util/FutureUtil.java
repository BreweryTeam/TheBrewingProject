package dev.jsinco.brewery.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FutureUtil {

    private FutureUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> CompletableFuture<List<T>> mergeFutures(List<CompletableFuture<T>> completableFutureList) {
        return CompletableFuture.allOf(completableFutureList.toArray(CompletableFuture<?>[]::new))
                .thenApplyAsync(ignored -> completableFutureList.stream().map(CompletableFuture::join).toList());
    }
}
