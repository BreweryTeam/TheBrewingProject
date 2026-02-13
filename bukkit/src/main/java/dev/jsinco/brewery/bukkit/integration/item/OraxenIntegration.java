package dev.jsinco.brewery.bukkit.integration.item;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class OraxenIntegration implements ItemIntegration, Listener {

    private static final boolean ENABLED = ClassUtil.exists("io.th0rgal.oraxen.api.OraxenItem");
    private final CompletableFuture<Void> initializedFuture = new CompletableFuture<>();

    @Override
    public Optional<ItemStack> createItem(String id) {
        return Optional.ofNullable(OraxenItems.getItemById(id))
                .map(ItemBuilder::build);
    }

    @Override
    public boolean isIngredient(String id) {
        return OraxenItems.getItemById(id) != null;
    }

    public @Nullable Component displayName(String oraxenId) {
        return OraxenItems.getOptionalItemById(oraxenId)
                .map(ItemBuilder::getDisplayName)
                .map(Component::text)
                .orElse(null);
    }

    @Override
    public @Nullable String getItemId(ItemStack itemStack) {
        return OraxenItems.getIdByItem(itemStack);
    }

    @Override
    public @NonNull CompletableFuture<Void> initialized() {
        return initializedFuture;
    }

    @Override
    public boolean isEnabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "oraxen";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
    }

    @EventHandler
    public void onOraxenItemsLoaded(OraxenItemsLoadedEvent event) {
        initializedFuture.completeAsync(() -> null);
    }

}
