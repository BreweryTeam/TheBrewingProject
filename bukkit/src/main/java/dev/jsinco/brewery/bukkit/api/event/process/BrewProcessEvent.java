package dev.jsinco.brewery.bukkit.api.event.process;

import dev.jsinco.brewery.api.brew.Brew;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class BrewProcessEvent extends Event implements Cancellable {

    @Getter
    @Setter
    private boolean cancelled;

    /**
     * The brew that is being processed.
     */
    @Getter
    private final Brew source;
    /**
     * The brew that will be created.
     */
    @Getter
    @Setter
    private Brew result;

    public BrewProcessEvent(Brew source, Brew result) {
        this.source = source;
        this.result = result;
    }

}
