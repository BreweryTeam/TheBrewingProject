package dev.jsinco.brewery.database;

public class UncheckedPersistenceException extends RuntimeException {
    public UncheckedPersistenceException(Throwable throwable) {
        super(throwable);
    }
}
