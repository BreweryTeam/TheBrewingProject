package dev.jsinco.brewery.database;

@FunctionalInterface
public interface PersistenceSupplier<T> {

    T get() throws PersistenceException;

    default T getUnchecked() throws UncheckedPersistenceException {
        try {
            return get();
        } catch (PersistenceException e) {
            throw new UncheckedPersistenceException(e);
        }
    }
}
