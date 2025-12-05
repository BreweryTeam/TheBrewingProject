package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransaction;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BarrelExtractEvent extends PermissibleBreweryEvent implements ItemTransactionEvent<ItemSource.ItemBasedSource> {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final BukkitBarrel barrel;
    @Getter
    private final @Nullable Player player;
    @Getter
    private final ItemTransactionSession<ItemSource.ItemBasedSource> transactionSession;


    public BarrelExtractEvent(BukkitBarrel barrel, ItemTransactionSession<ItemSource.ItemBasedSource> transactionSession, @Nullable Player player) {
        this.barrel = barrel;
        this.transactionSession = transactionSession;
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
