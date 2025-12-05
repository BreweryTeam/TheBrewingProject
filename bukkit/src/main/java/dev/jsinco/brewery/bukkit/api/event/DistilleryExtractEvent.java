package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DistilleryExtractEvent extends PermissibleBreweryEvent implements ItemTransactionEvent<ItemSource.ItemBasedSource> {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final BukkitDistillery distillery;
    @Getter
    private final ItemTransactionSession<ItemSource.ItemBasedSource> transactionSession;
    @Getter
    private final @Nullable Player player;


    public DistilleryExtractEvent(BukkitDistillery distillery, ItemTransactionSession<ItemSource.ItemBasedSource> transactionSession, @Nullable Player player) {
        this.distillery = distillery;
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
