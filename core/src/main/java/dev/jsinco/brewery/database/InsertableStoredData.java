package dev.jsinco.brewery.database;

public interface InsertableStoredData<T, C> {
    void insert(T value, C connection) throws PersistenceException;


}
