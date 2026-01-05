package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.api.util.CancelState;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CauldronDestroyEvent extends PermissibleBreweryEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The cauldron that was destroyed.
     */
    @Getter
    private final Cauldron cauldron;
    /**
     * The player that destroyed the cauldron. Will be null if the cauldron was destroyed by an explosion, piston,
     * or any non-player source.
     */
    @Getter
    private final @Nullable Player player;
    /**
     * The location of the cauldron.
     */
    @Getter
    private final Location location;

    public CauldronDestroyEvent(CancelState state, Cauldron cauldron, @Nullable Player player, Location location) {
        super(state);
        this.cauldron = cauldron;
        this.player = player;
        this.location = location;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
