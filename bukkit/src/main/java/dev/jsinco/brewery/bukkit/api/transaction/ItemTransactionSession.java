package dev.jsinco.brewery.bukkit.api.transaction;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

public class ItemTransactionSession<T extends ItemSource> {

    @Getter
    public ItemTransaction transaction;
    @Getter
    @Setter
    public boolean cancelled;
    @Setter
    @Getter
    public T result;


    public ItemTransactionSession(ItemTransaction transaction, boolean cancelled, T result) {
        this.transaction = Preconditions.checkNotNull(transaction);
        this.cancelled = cancelled;
        this.result = Preconditions.checkNotNull(result);
    }
}
