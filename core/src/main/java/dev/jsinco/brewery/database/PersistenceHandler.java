package dev.jsinco.brewery.database;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public interface PersistenceHandler<C> {

    default <T> List<T> retrieveAllNow(RetrievableStoredData<T, C> dataType) throws PersistenceException {
        try {
            return retrieveAll(dataType).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof PersistenceException persistenceException) {
                throw persistenceException;
            }
            throw new PersistenceException(e.getCause());
        }
    }

    <T> CompletableFuture<List<T>> retrieveAll(RetrievableStoredData<T, C> dataType) throws
            PersistenceException;

    <T> CompletableFuture<Void> remove(RemovableStoredData<T, C> dataType, T toRemove) throws PersistenceException;

    <T> CompletableFuture<Void> updateValue(UpdateableStoredData<T, C> dataType, T newValue) throws PersistenceException;

    <T> CompletableFuture<Void> insertValue(InsertableStoredData<T, C> dataType, T value) throws PersistenceException;

    default <T, U> List<T> findNow(FindableStoredData<T, U, C> dataType, U searchObject) throws
            PersistenceException {
        try {
            return find(dataType, searchObject).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof PersistenceException persistenceException) {
                throw persistenceException;
            }
            throw new PersistenceException(e.getCause());
        }
    }

    <T, U> CompletableFuture<List<T>> find(FindableStoredData<T, U, C> dataType, U searchObject) throws
            PersistenceException;

    default <T> T getSingletonNow(SingletonStoredData<T, C> dataType) throws PersistenceException {
        try {
            return getSingleton(dataType).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof PersistenceException persistenceException) {
                throw persistenceException;
            }
            throw new PersistenceException(e.getCause());
        }
    }

    <T> CompletableFuture<T> getSingleton(SingletonStoredData<T, C> dataType) throws PersistenceException;

    <T> CompletableFuture<Void> setSingleton(SingletonStoredData<T, C> dataType, T t) throws PersistenceException;

    CompletableFuture<Void> flush();
}
