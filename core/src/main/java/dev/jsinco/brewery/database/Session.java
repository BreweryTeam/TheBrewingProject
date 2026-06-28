package dev.jsinco.brewery.database;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface Session<T extends Session<T>> {

    Executor executor();

    default <U> CompletableFuture<U> fetch(PersistenceSupplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier::getUnchecked, executor());
    }

    default CompletableFuture<Void> execute(PersistenceRunnable runnable) {
        return CompletableFuture.runAsync(runnable::runUnchecked, executor());
    }
}
