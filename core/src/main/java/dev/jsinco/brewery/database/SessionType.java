package dev.jsinco.brewery.database;

import java.util.concurrent.Executor;

@FunctionalInterface
public interface SessionType<T extends Session<T>> {

    T retrieve(Executor executor, PersistenceHandler handler) throws PersistenceException;
}
