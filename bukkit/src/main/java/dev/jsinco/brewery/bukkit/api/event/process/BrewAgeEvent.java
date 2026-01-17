package dev.jsinco.brewery.bukkit.api.event.process;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.BarrelAccess;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

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
