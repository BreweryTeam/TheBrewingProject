package dev.jsinco.brewery.bukkit.effect;

import java.util.concurrent.CancellationException;

public class EventCancelledException extends CancellationException {

    private final int index;

    public EventCancelledException(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
