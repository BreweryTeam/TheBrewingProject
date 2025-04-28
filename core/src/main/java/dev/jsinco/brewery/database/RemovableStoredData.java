package dev.jsinco.brewery.database;

public interface RemovableStoredData<T, C> {

    void remove(T toRemove, C connection) throws PersistenceException;

}
