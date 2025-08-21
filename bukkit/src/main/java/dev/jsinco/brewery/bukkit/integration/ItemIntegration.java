package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.bukkit.ingredient.PluginIngredient;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ItemIntegration extends Integration {

    Optional<ItemStack> createItem(String id);

    default CompletableFuture<Optional<Ingredient>> createIngredient(String id) {
        return initialized()
                .handleAsync((ignored1, exception) -> {
                            if (exception != null) {
                                Logger.logErr("Couldn't create PluginIngredient for id '" + getId() + "'.");
                                Logger.logErr(exception);
                                return Optional.empty();
                            }
                            return Optional.of(new PluginIngredient(new BreweryKey(getId(), id), this));
                        }
                );
    }

    @Nullable Component displayName(String id);

    @Nullable String itemId(ItemStack itemStack);

    CompletableFuture<Void> initialized();

    default Optional<Ingredient> getIngredient(@NotNull ItemStack itemStack) {
        return Optional.ofNullable(itemId(itemStack))
                .map(id -> new PluginIngredient(new BreweryKey(getId(), id), this));
    }
}
