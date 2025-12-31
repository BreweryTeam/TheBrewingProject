package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.breweries.DistilleryAccess;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DistilleryInsertEvent extends PermissibleBreweryEvent implements ItemTransactionEvent<ItemSource.BrewBasedSource> {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final DistilleryAccess distillery;
    @Getter
    private final @Nullable Player player;
    @Getter
    private final ItemTransactionSession<ItemSource.BrewBasedSource> transactionSession;


    public DistilleryInsertEvent(DistilleryAccess distillery, ItemTransactionSession<ItemSource.BrewBasedSource> transactionSession,
                                 @NotNull CancelState state, @Nullable Player player) {
        super(state);
        this.distillery = distillery;
        this.transactionSession = transactionSession;
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
