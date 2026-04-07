package dev.jsinco.brewery.bukkit.api.event.transaction;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.bukkit.api.event.PermissibleBreweryEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CauldronInsertEvent extends PermissibleBreweryEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Cauldron cauldron;
    private ItemSource.ItemBasedSource itemSource;
    private final @Nullable Player player;

    public CauldronInsertEvent(Cauldron cauldron, ItemSource.ItemBasedSource itemSource,
                               dev.jsinco.brewery.api.util.@NonNull CancelState state, @Nullable Player player) {
        super(state);
        this.cauldron = cauldron;
        this.itemSource = itemSource;
        this.player = player;
    }

    public void setResult(@NonNull ItemStack item) {
        Preconditions.checkNotNull(item);
        itemSource = new ItemSource.ItemBasedSource(item.clone());
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Cauldron getCauldron() {
        return this.cauldron;
    }

    public ItemSource.ItemBasedSource getItemSource() {
        return this.itemSource;
    }

    @Nullable
    public Player getPlayer() {
        return this.player;
    }
}
