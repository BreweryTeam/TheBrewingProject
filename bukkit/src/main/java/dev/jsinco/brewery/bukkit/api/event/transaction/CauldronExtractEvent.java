package dev.jsinco.brewery.bukkit.api.event.transaction;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.bukkit.api.event.PermissibleBreweryEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CauldronExtractEvent extends PermissibleBreweryEvent {

    @Getter
    private final Cauldron cauldron;
    /**
     * Use {@link #getItemResult()} instead. Will cause weird issues with newer api
     */
    @Getter
    @Deprecated(forRemoval = true)
    private ItemSource.BrewBasedSource brewSource;
    @Getter
    private ItemSource itemResult;
    @Getter
    private final @Nullable Player player;

    public CauldronExtractEvent(Cauldron cauldron, ItemSource.BrewBasedSource brewSource,
                                @NotNull dev.jsinco.brewery.api.util.CancelState state, @Nullable Player player) {
        super(state);
        this.cauldron = cauldron;
        this.brewSource = brewSource;
        this.itemResult = brewSource;
        this.player = player;
    }

    /**
     * @param brew The brew to set
     */
    public void setResult(@NotNull Brew brew) {
        Preconditions.checkNotNull(brew);
        brewSource = new ItemSource.BrewBasedSource(brew, new Brew.State.Other());
        itemResult = brewSource;
    }

    /**
     * Set resulting item
     *
     * @param result The item to set
     */
    public void setItemResult(@NotNull ItemStack result) {
        Preconditions.checkNotNull(result);
        itemResult = new ItemSource.ItemBasedSource(result);
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
