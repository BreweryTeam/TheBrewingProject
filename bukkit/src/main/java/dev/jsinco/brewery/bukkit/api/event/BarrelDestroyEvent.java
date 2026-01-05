package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.breweries.BarrelAccess;
import dev.jsinco.brewery.api.util.CancelState;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BarrelDestroyEvent extends PermissibleBreweryEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The barrel that was destroyed.
     */
    @Getter
    private final BarrelAccess barrel;
    /**
     * The player that destroyed the barrel. Will be null if the barrel was destroyed by an explosion, piston,
     * or any non-player source.
     */
    @Getter
    private final @Nullable Player player;
    /**
     * The location of the block that was destroyed or changed. If multiple blocks were destroyed,
     * such as by an explosion, then an arbitrary block is chosen.
     */
    @Getter
    private final Location location;

    public BarrelDestroyEvent(CancelState state, BarrelAccess barrel, @Nullable Player player, Location location) {
        super(state);
        this.barrel = barrel;
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
