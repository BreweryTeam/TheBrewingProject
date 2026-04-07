package dev.jsinco.brewery.bukkit.api.event.process;

import dev.jsinco.brewery.api.brew.Brew;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jspecify.annotations.NonNull;

public abstract class BrewProcessEvent extends Event implements Cancellable {

    private boolean cancelled;

    /**
     * The brew to be processed
     */
    @NonNull
    private final Brew source;

    /**
     * The brew
     */
    @NonNull
    private Brew result;

    public BrewProcessEvent(@NonNull Brew source, @NonNull Brew result) {
        this.source = source;
        this.result = result;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public @NonNull Brew getSource() {
        return this.source;
    }

    public @NonNull Brew getResult() {
        return this.result;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setResult(@NonNull Brew result) {
        this.result = result;
    }
}
