package dev.jsinco.brewery.bukkit.api.event.structure;

import dev.jsinco.brewery.api.breweries.DistilleryAccess;
import dev.jsinco.brewery.api.util.CancelState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class DistilleryCreateEvent extends BreweryCreateEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final DistilleryAccess distillery;

    public DistilleryCreateEvent(CancelState state, @Nullable Player player, Location location, DistilleryAccess distillery) {
        super(state, player, location);
        this.distillery = distillery;
    }

    public DistilleryAccess getDistillery() {
        return distillery;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
