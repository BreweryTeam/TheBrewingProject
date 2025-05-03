package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.bukkit.ingredient.PluginIngredient;
import dev.jsinco.brewery.util.BreweryKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface ItemIntegration extends Integration {

    Optional<ItemStack> createItem(String id);

    default CompletableFuture<Optional<PluginIngredient>> createIngredient(String id) {
        return initialized().orTimeout(10, TimeUnit.SECONDS)
                .handleAsync((ignored1, exception) ->
                        Optional.ofNullable(exception).map(ignored2 -> new PluginIngredient(new BreweryKey(getId(), id), this))
                );
    }

    @Nullable String displayName(String id);

    @Nullable String itemId(ItemStack itemStack);

    CompletableFuture<Void> initialized();
}
