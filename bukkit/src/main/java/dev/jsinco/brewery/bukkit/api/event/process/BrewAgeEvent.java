package dev.jsinco.brewery.bukkit.api.event.process;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.BarrelAccess;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event that triggers whenever a brew ages. This event could be triggered multiple times for one brew when it's aging
 * in one barrel, and it can only occur when the barrel inventory is open.
 */
public class BrewAgeEvent extends BrewProcessEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final BarrelAccess barrel;

    public BrewAgeEvent(BarrelAccess barrel, Brew source, Brew result) {
        super(source, result);
        this.barrel = barrel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
