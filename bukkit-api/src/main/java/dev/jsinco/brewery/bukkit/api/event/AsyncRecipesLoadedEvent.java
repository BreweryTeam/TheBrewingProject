package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

/**
 * An event that triggers <b>asynchronously</b> whenever all recipes have been loaded.
 */
public final class AsyncRecipesLoadedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final RecipeRegistry<ItemStack> recipes;

    public AsyncRecipesLoadedEvent(RecipeRegistry<ItemStack> recipes) {
        super(true);
        this.recipes = recipes;
    }

    /**
     * @return The recipe registry filled with recipes
     */
    public RecipeRegistry<ItemStack> recipesRegistry() {
        return recipes;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
