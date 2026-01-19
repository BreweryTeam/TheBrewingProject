package dev.jsinco.brewery.bukkit.api.event.process;

import dev.jsinco.brewery.api.brew.Brew;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class BrewProcessEvent extends Event implements Cancellable {

    @Getter
    @Setter
    private boolean cancelled;

    /**
     * The brew to be processed
     */
    @Getter
    @NonNull
    private final Brew source;

    /**
     * The brew
     */
    @Getter
    @Setter
    @NonNull
    private Brew result;

    public BrewProcessEvent(@NonNull Brew source, @NonNull Brew result) {
        this.source = source;
        this.result = result;
    }

}
