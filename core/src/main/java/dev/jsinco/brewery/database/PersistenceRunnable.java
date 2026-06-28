package dev.jsinco.brewery.database;

@FunctionalInterface
public interface PersistenceRunnable {

    void run() throws PersistenceException;

    default void runUnchecked() throws UncheckedPersistenceException {
        try {
            run();
        } catch (PersistenceException e) {
            throw new UncheckedPersistenceException(e);
        }
    }
}
