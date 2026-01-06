package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.BarrelAccess;
import dev.jsinco.brewery.api.util.CancelState;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    /**
     * The brews that will be dropped when the barrel is broken. Can be modified.
     */
    @Getter
    private List<Brew> drops;

    public BarrelDestroyEvent(CancelState state, BarrelAccess barrel, @Nullable Player player, Location location, Collection<Brew> drops) {
        super(state);
        this.barrel = barrel;
        this.player = player;
        this.location = location;
        setDrops(drops);
    }

    /**
     * Replaces the list of drops with the provided collection.
     * @param drops collection of brews to drop
     */
    public void setDrops(Collection<Brew> drops) {
        this.drops = new ArrayList<>(drops);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
