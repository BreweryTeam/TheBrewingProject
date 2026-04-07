package dev.jsinco.brewery.bukkit.structure;

public class StructureReadException extends IllegalArgumentException {

    public StructureReadException(String message) {
        super(message);
    }

    public StructureReadException(Throwable throwable) {
        super(throwable);
    }

    public StructureReadException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
