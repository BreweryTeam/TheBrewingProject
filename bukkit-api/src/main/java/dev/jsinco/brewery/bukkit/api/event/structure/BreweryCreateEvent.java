package dev.jsinco.brewery.bukkit.api.event.structure;

import dev.jsinco.brewery.api.util.CancelState;
import dev.jsinco.brewery.bukkit.api.event.PermissibleBreweryEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class BreweryCreateEvent extends PermissibleBreweryEvent {

    private final Player player;
    private final Location location;

    public BreweryCreateEvent(CancelState state, Player player, Location location) {
        super(state);
        this.player = player;
        this.location = location;
    }

    /**
     * @return The player that created the structure
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return The location the structure was created
     */
    public Location getLocation() {
        return this.location;
    }
}
