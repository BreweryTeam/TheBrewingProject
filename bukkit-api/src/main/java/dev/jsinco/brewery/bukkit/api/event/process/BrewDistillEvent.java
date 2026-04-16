package dev.jsinco.brewery.bukkit.api.event.process;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.DistilleryAccess;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

/**
 * An event that triggers whenever a brew distills. Will only trigger when an inventory is open.
 */
public class BrewDistillEvent extends BrewProcessEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final DistilleryAccess distillery;

    public BrewDistillEvent(DistilleryAccess distillery, Brew source, Brew result) {
        super(source, result);
        this.distillery = distillery;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public DistilleryAccess getDistillery() {
        return this.distillery;
    }
}
