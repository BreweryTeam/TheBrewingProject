package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;

public interface ItemTransactionEvent<T extends ItemSource> {

    /**
     * @return A session for an item transaction
     */
    ItemTransactionSession<T> getTransactionSession();

    /**
     * Calls the event and tests if cancelled.
     *
     * @return {@code false} if event was cancelled, if cancellable. otherwise {@code true}.
     */
    boolean callEvent();

    /**
     *
     * @return The cancel state of the item transaction
     */
    dev.jsinco.brewery.api.util.CancelState getCancelState();

    /**
     * @param state The new cancel state for the item transaction
     */
    void setCancelState(dev.jsinco.brewery.api.util.CancelState state);
}
