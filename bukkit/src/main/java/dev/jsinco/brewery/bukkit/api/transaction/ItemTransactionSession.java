package dev.jsinco.brewery.bukkit.api.transaction;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
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
    @Getter
    private @Nullable T result;


    public ItemTransactionSession(ItemTransaction transaction, @Nullable T result) {
        this.transaction = Preconditions.checkNotNull(transaction);
        setResult(result);
    }

    @SuppressWarnings("unchecked")
    public void setResult(@Nullable T result) {
        if (result instanceof ItemSource.ItemBasedSource(ItemStack itemStack)) {
            // Since result: T is instanceof ItemBasedSource and ItemBasedSource is final,
            // ItemBasedSource must be T
            this.result = (T) new ItemSource.ItemBasedSource(itemStack.clone());
        } else {
            this.result = result;
        }
    }
}
