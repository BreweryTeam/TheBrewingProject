package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.bukkit.ingredient.PluginIngredient;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.util.BreweryKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface ItemIntegration extends Integration {

    Optional<ItemStack> createItem(String id);

    default CompletableFuture<Optional<Ingredient>> createIngredient(String id) {
        return initialized().orTimeout(10, TimeUnit.SECONDS)
                .handleAsync((ignored1, exception) -> {
                            if (exception != null) {
                                return Optional.empty();
                            }
                            return Optional.of(new PluginIngredient(new BreweryKey(getId(), id), this));
                        }
                );
    }

    @Nullable String displayName(String id);

    @Nullable String itemId(ItemStack itemStack);

    CompletableFuture<Void> initialized();

    default Optional<Ingredient> getIngredient(@NotNull ItemStack itemStack) {
        return Optional.ofNullable(itemId(itemStack))
                .map(id -> new PluginIngredient(new BreweryKey(getId(), id), this));
    }
}
