package dev.jsinco.brewery.bukkit.api.event.process;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.DistilleryAccess;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BrewDistillEvent extends BrewProcessEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final DistilleryAccess distillery;

    public BrewDistillEvent(DistilleryAccess distillery, Brew source, Brew result) {
        super(source, result);
        this.distillery = distillery;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
