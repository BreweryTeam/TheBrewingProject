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

public class BarrelDestroyEvent extends BreweryDestroyEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The barrel that was destroyed.
     */
    @Getter
    private final BarrelAccess barrel;
    /**
     * The brews that will be dropped when the barrel is broken. Can be modified.
     */
    @Getter
    private List<Brew> drops;

    public BarrelDestroyEvent(CancelState state, BarrelAccess barrel, @Nullable Player player, Location location, Collection<Brew> drops) {
        super(state, player, location);
        this.barrel = barrel;
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
