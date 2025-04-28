package dev.jsinco.brewery.database;

public interface UpdateableStoredData<T, C> {
    void update(T newValue, C connection) throws PersistenceException;

}
