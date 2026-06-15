package dev.jsinco.brewery.bukkit.api.event.structure;

import dev.jsinco.brewery.api.breweries.BarrelAccess;
import dev.jsinco.brewery.api.util.CancelState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class BarrelCreateEvent extends BreweryCreateEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final BarrelAccess barrel;

    public BarrelCreateEvent(CancelState state, @Nullable Player player, Location location, BarrelAccess barrel) {
        super(state, player, location);
        this.barrel = barrel;
    }

    /**
     * The barrel that was created.
     */
    public BarrelAccess getBarrel() {
        return this.barrel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
