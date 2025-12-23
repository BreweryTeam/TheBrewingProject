package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DistilleryInsertEvent extends PermissibleBreweryEvent implements ItemTransactionEvent<ItemSource.BrewBasedSource> {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final BukkitDistillery distillery;
    @Getter
    private final @Nullable Player player;
    @Getter
    private final ItemTransactionSession<ItemSource.BrewBasedSource> transactionSession;


    public DistilleryInsertEvent(BukkitDistillery distillery, ItemTransactionSession<ItemSource.BrewBasedSource> transactionSession,
                                 boolean cancelled, boolean denied, @Nullable Component denyMessage, @Nullable Player player) {
        super(cancelled, denied, denyMessage);
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
