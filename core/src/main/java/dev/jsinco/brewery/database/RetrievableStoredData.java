package dev.jsinco.brewery.database;

import java.util.List;

public interface RetrievableStoredData<T, C> {


    List<T> retrieveAll(C connection) throws PersistenceException;
}
