package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.recipe.RecipeEffects;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Called when a player drinks a brew, or consumes any other item that has recipe-based effects.
 * This event is not called when a player consumes vanilla items with added modifiers
 * (such as bread reducing alcohol and toxins).
 */
public class BrewConsumeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;
    /**
     * The player that consumed the item.
     */
    private final Player player;
    /**
     * The item that is being consumed. Modifying this item will have no effect.
     */
    private final ItemStack item;

    /**
     * The effects of the consumed item
     */
    private final RecipeEffects recipeEffects;
    /**
     * The hand used to consume the item.
     */
    private final EquipmentSlot hand;
    /**
     * The item that will replace the consumed item. Setting to null clears any custom replacement and will
     * instead use the default behavior.
     */
    private @Nullable ItemStack replacement;

    public BrewConsumeEvent(Player player, ItemStack item, EquipmentSlot hand, @Nullable ItemStack replacement, RecipeEffects effects) {
        this.player = player;
        this.item = item;
        this.hand = hand;
        this.replacement = replacement;
        this.recipeEffects = effects;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public RecipeEffects getRecipeEffects() {
        return this.recipeEffects;
    }

    public EquipmentSlot getHand() {
        return this.hand;
    }

    @Nullable
    public ItemStack getReplacement() {
        return this.replacement;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setReplacement(@Nullable ItemStack replacement) {
        this.replacement = replacement;
    }
}
