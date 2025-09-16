package dev.jsinco.brewery.bukkit.api.integration;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.api.ingredient.PluginIngredient;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ItemIntegration extends Integration {

    /**
     * Creates an ItemStack from the given item identifier
     */
    Optional<ItemStack> createItem(String id);

    /**
     * @return True if the id is a valid ingredient key to this item integration
     */
    boolean isIngredient(String id);

    /**
     * Creates an Ingredient from the given item identifier
     */
    default CompletableFuture<Optional<Ingredient>> createIngredient(String id) {
        return initialized()
                .handleAsync((ignored1, exception) -> {
                            if (exception != null) {
                                Logger.logErr("Couldn't create PluginIngredient '" + id + "' for item integration " + getId());
                                Logger.logErr(exception);
                                return Optional.empty();
                            }
                            if (!isIngredient(id)) {
                                Logger.logErr("Couldn't create PluginIngredient '" + id + "' for item integration " + getId());
                                return Optional.empty();
                            }
                            return Optional.of(new PluginIngredient(new BreweryKey(getId(), id), this));
                        }
                );
    }

    /**
     * Returns the display name of the item with the given identifier
     */
    @Nullable Component displayName(String id);

    /**
     * Returns the identifier of the given ItemStack, or null if unknown
     */
    @Nullable String getItemId(ItemStack itemStack);

    /**
     * Completes when the integration has finished initializing
     */
    CompletableFuture<Void> initialized();

    /**
     * Gets the Ingredient representation of the given ItemStack
     */
    default Optional<Ingredient> getIngredient(@NotNull ItemStack itemStack) {
        return Optional.ofNullable(getItemId(itemStack))
                .map(id -> new PluginIngredient(new BreweryKey(getId(), id), this));
    }
}
