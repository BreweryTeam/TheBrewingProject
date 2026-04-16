package dev.jsinco.brewery.bukkit.api.event.structure;

import dev.jsinco.brewery.api.util.CancelState;
import dev.jsinco.brewery.bukkit.api.event.PermissibleBreweryEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public abstract class BreweryDestroyEvent extends PermissibleBreweryEvent {

    /**
     * The player that destroyed the structure. Will be null if the structure was destroyed by an explosion, piston,
     * or any non-player source.
     */
    private final @Nullable Player player;
    /**
     * The location of the block that was destroyed or changed. If multiple blocks were destroyed,
     * such as by an explosion, then an arbitrary block is chosen.
     */
    private final Location location;

    public BreweryDestroyEvent(CancelState state, @Nullable Player player, Location location) {
        super(state);
        this.player = player;
        this.location = location;
    }

    @Nullable
    public Player getPlayer() {
        return this.player;
    }

    public Location getLocation() {
        return this.location;
    }
}
