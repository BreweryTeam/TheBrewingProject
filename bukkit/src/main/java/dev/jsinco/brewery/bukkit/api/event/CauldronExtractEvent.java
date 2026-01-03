package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CauldronExtractEvent extends PermissibleBreweryEvent {

    @Getter
    private final Cauldron cauldron;
    @Getter
    private final ItemSource.BrewBasedSource brewSource;
    @Getter
    private final @Nullable Player player;

    public CauldronExtractEvent(Cauldron cauldron, ItemSource.BrewBasedSource brewSource,
                                @NotNull dev.jsinco.brewery.api.util.CancelState state, @Nullable Player player) {
        super(state);
        this.cauldron = cauldron;
        this.brewSource = brewSource;
        this.player = player;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
