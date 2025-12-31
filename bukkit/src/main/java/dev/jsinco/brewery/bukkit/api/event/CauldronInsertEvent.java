package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CauldronInsertEvent extends PermissibleBreweryEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final Cauldron cauldron;
    @Getter
    private final ItemSource.ItemBasedSource itemSource;
    @Getter
    private final @Nullable Player player;

    public CauldronInsertEvent(Cauldron cauldron, ItemSource.ItemBasedSource itemSource,
                               @NotNull CancelState state, @Nullable Player player) {
        super(state);
        this.cauldron = cauldron;
        this.itemSource = itemSource;
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerLists() {
        return HANDLERS;
    }
}
