package dev.jsinco.brewery.bukkit.api.event.structure;

import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.api.util.CancelState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class CauldronCreateEvent extends BreweryCreateEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Cauldron cauldron;

    public CauldronCreateEvent(CancelState state, @Nullable Player player, Location location, Cauldron cauldron) {
        super(state, player, location);
        this.cauldron = cauldron;
    }

    /**
     * @return The cauldron that was created
     */
    public Cauldron getCauldron() {
        return this.cauldron;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
