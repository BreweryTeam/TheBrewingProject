package dev.jsinco.brewery.bukkit.api.transaction;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class ItemTransactionSession<T extends ItemSource> {

    /**
     * The transaction
     */
    @Getter
    private ItemTransaction transaction;

    /**
     * The item in the transaction
     */
    @Setter
    @Getter
    private @Nullable T result;


    public ItemTransactionSession(ItemTransaction transaction, @Nullable T result) {
        this.transaction = Preconditions.checkNotNull(transaction);
        this.result = result;
    }
}
