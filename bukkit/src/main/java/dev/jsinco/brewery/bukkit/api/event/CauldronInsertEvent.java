package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CauldronInsertEvent extends PermissibleBreweryEvent implements ItemTransactionEvent<ItemSource.ItemBasedSource> {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final BukkitCauldron cauldron;
    @Getter
    private final ItemTransactionSession<ItemSource.ItemBasedSource> transactionSession;

    public CauldronInsertEvent(BukkitCauldron cauldron, ItemTransactionSession<ItemSource.ItemBasedSource> transactionSession, boolean cancelled, @Nullable Player player) {
        super(cancelled);
        this.cauldron = cauldron;
        this.transactionSession = transactionSession;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerLists() {
        return HANDLERS;
    }
}
