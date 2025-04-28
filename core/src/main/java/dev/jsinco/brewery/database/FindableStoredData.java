package dev.jsinco.brewery.database;

import java.util.List;

public interface FindableStoredData<T, U, C> {

    List<T> find(U searchObject, C connection) throws PersistenceException;
}
