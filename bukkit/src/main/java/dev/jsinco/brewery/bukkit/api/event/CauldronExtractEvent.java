package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CauldronExtractEvent extends PermissibleBreweryEvent implements ItemTransactionEvent<ItemSource.ItemBasedSource> {

    @Getter
    private final BukkitCauldron cauldron;
    @Getter
    private final Player player;
    @Getter
    private ItemTransactionSession<ItemSource.ItemBasedSource> transactionSession;

    public CauldronExtractEvent(BukkitCauldron cauldron, ItemTransactionSession<ItemSource.ItemBasedSource> transactionSession, @Nullable Player player) {
        this.cauldron = cauldron;
        this.transactionSession = transactionSession;
        this.player = player;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerLists() {
        return HANDLERS;
    }
}
