package dev.jsinco.brewery.bukkit.api.event.structure;

import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.api.util.CancelState;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CauldronDestroyEvent extends BreweryDestroyEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The cauldron that was destroyed.
     */
    @Getter
    private final Cauldron cauldron;

    public CauldronDestroyEvent(CancelState state, Cauldron cauldron, @Nullable Player player, Location location) {
        super(state, player, location);
        this.cauldron = cauldron;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
