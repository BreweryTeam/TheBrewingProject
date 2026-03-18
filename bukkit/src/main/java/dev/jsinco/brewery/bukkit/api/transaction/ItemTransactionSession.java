package dev.jsinco.brewery.bukkit.api.transaction;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemTransactionSession<T extends ItemSource> {

    /**
     * The transaction
     */
    private ItemTransaction transaction;

    /**
     * The item in the transaction
     */
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

    public ItemTransaction getTransaction() {
        return this.transaction;
    }

    @Nullable
    public T getResult() {
        return this.result;
    }
}
