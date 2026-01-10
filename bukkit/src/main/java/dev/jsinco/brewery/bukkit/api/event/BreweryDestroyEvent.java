package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.util.CancelState;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public abstract class BreweryDestroyEvent extends PermissibleBreweryEvent {

    /**
     * The player that destroyed the structure. Will be null if the structure was destroyed by an explosion, piston,
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

    public BreweryDestroyEvent(CancelState state, @Nullable Player player, Location location) {
        super(state);
        this.player = player;
        this.location = location;
    }

}
