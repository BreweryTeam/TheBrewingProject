package dev.jsinco.brewery.database;

import dev.jsinco.brewery.database.sql.DatabaseDriver;

import java.util.concurrent.CompletableFuture;

public interface PersistenceHandler {

    CompletableFuture<Void> flush();

    <T extends Session<T>> T startSession(SessionType<T> sessionType) throws PersistenceException;

    DatabaseDriver driver();
}
