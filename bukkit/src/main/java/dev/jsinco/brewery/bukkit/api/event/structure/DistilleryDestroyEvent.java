package dev.jsinco.brewery.bukkit.api.event.structure;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.DistilleryAccess;
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

public class DistilleryDestroyEvent extends BreweryDestroyEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The distillery that was destroyed.
     */
    @Getter
    private final DistilleryAccess distillery;
    /**
     * The brews that will be dropped when the distillery is broken. Can be modified.
     */
    @Getter
    private List<Brew> drops;

    public DistilleryDestroyEvent(CancelState state, DistilleryAccess distillery, @Nullable Player player, Location location, Collection<Brew> drops) {
        super(state, player, location);
        this.distillery = distillery;
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
