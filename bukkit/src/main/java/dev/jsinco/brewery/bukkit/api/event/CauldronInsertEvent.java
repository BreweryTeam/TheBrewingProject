package dev.jsinco.brewery.bukkit.api.event;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CauldronInsertEvent extends PermissibleBreweryEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final Cauldron cauldron;
    @Getter
    private ItemSource.ItemBasedSource itemSource;
    @Getter
    private final @Nullable Player player;

    public CauldronInsertEvent(Cauldron cauldron, ItemSource.ItemBasedSource itemSource,
                               @NotNull dev.jsinco.brewery.api.util.CancelState state, @Nullable Player player) {
        super(state);
        this.cauldron = cauldron;
        this.itemSource = itemSource;
        this.player = player;
    }

    public void setResult(@NotNull ItemStack item) {
        Preconditions.checkNotNull(item);
        itemSource = new ItemSource.ItemBasedSource(item.clone());
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
