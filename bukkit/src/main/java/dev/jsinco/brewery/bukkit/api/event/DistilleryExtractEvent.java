package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.breweries.DistilleryAccess;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DistilleryExtractEvent extends PermissibleBreweryEvent implements ItemTransactionEvent<ItemSource.ItemBasedSource> {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final DistilleryAccess distillery;
    @Getter
    private final ItemTransactionSession<ItemSource.ItemBasedSource> transactionSession;
    @Getter
    private final @Nullable Player player;


    public DistilleryExtractEvent(DistilleryAccess distillery, ItemTransactionSession<ItemSource.ItemBasedSource> transactionSession,
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
