package dev.jsinco.brewery.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FutureUtil {

    private FutureUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> CompletableFuture<List<T>> mergeFutures(List<CompletableFuture<T>> completableFutureList) {
        List<T> unwrappedFutures = new ArrayList<>(completableFutureList.size());
        CompletableFuture<?>[] completableFutures = new CompletableFuture<?>[completableFutureList.size()];
        for (int i = 0; i < completableFutureList.size(); i++) {
            final int iFinal = i;
            completableFutures[i] = completableFutureList.get(i).thenAcceptAsync(t -> unwrappedFutures.set(iFinal, t));
        }
        return CompletableFuture.allOf(completableFutures)
                .thenApplyAsync(ignored -> unwrappedFutures);
    }
}
