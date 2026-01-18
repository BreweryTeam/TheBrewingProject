package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.recipe.RecipeEffects;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player drinks a brew, or consumes any other item that has recipe-based effects.
 * This event is not called when a player consumes vanilla items with added modifiers
 * (such as bread reducing alcohol and toxins).
 */
public class BrewConsumeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    @Setter
    private boolean cancelled;
    /**
     * The player that consumed the item.
     */
    @Getter
    private final Player player;
    /**
     * The item that is being consumed. Modifying this item will have no effect.
     */
    @Getter
    private final ItemStack item;

    /**
     * The effects of the consumed item
     */
    @Getter
    private final RecipeEffects recipeEffects;
    /**
     * The hand used to consume the item.
     */
    @Getter
    private final EquipmentSlot hand;
    /**
     * The item that will replace the consumed item. Setting to null clears any custom replacement and will
     * instead use the default behavior.
     */
    @Getter
    @Setter
    private @Nullable ItemStack replacement;

    public BrewConsumeEvent(Player player, ItemStack item, EquipmentSlot hand, @Nullable ItemStack replacement, RecipeEffects effects) {
        this.player = player;
        this.item = item;
        this.hand = hand;
        this.replacement = replacement;
        this.recipeEffects = effects;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
