package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;

public interface ItemTransactionEvent<T extends ItemSource>{

    ItemTransactionSession<T> getTransactionSession();
}
