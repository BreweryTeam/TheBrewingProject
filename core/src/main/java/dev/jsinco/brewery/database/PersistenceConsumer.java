package dev.jsinco.brewery.database;

@FunctionalInterface
public interface PersistenceConsumer<T> {

    T get() throws PersistenceException;
}
