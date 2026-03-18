package dev.jsinco.brewery.bukkit.api.event.transaction;

import dev.jsinco.brewery.api.breweries.BarrelAccess;
import dev.jsinco.brewery.bukkit.api.event.PermissibleBreweryEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BarrelInsertEvent extends PermissibleBreweryEvent implements ItemTransactionEvent<ItemSource.BrewBasedSource> {

    private static final HandlerList HANDLERS = new HandlerList();
    private final BarrelAccess barrel;
    private final @Nullable Player player;
    private final ItemTransactionSession<ItemSource.BrewBasedSource> transactionSession;

    public BarrelInsertEvent(BarrelAccess barrel, ItemTransactionSession<ItemSource.BrewBasedSource> transactionSession,
                             dev.jsinco.brewery.api.util.@NonNull CancelState state, @Nullable Player player) {
        super(state);
        this.barrel = barrel;
        this.transactionSession = transactionSession;
        this.player = player;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public BarrelAccess getBarrel() {
        return this.barrel;
    }

    @Nullable
    public Player getPlayer() {
        return this.player;
    }

    public ItemTransactionSession<ItemSource.BrewBasedSource> getTransactionSession() {
        return this.transactionSession;
    }
}
