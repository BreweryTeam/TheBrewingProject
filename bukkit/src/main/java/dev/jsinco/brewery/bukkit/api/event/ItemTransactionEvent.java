package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;

public interface ItemTransactionEvent<T extends ItemSource> extends Permissible {

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
}
