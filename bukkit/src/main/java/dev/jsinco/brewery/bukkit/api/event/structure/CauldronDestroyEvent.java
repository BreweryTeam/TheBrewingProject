package dev.jsinco.brewery.bukkit.api.event.structure;

import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.api.util.CancelState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CauldronDestroyEvent extends BreweryDestroyEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The cauldron that was destroyed.
     */
    private final Cauldron cauldron;

    public CauldronDestroyEvent(CancelState state, Cauldron cauldron, @Nullable Player player, Location location) {
        super(state, player, location);
        this.cauldron = cauldron;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Cauldron getCauldron() {
        return this.cauldron;
    }
}
