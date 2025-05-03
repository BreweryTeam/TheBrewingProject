package dev.jsinco.brewery.bukkit.integration.item;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.integration.ItemIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class OraxenHook implements ItemIntegration, Listener {

    private static final boolean ENABLED = ClassUtil.exists("io.th0rgal.oraxen.api.OraxenItem");
    private CompletableFuture<Void> initializedFuture;

    @Override
    public Optional<ItemStack> createItem(String id) {
        return Optional.ofNullable(OraxenItems.getItemById(id))
                .map(ItemBuilder::build);
    }

    public @Nullable String displayName(String oraxenId) {
        return OraxenItems.exists(oraxenId) ? OraxenItems.getItemById(oraxenId).getDisplayName() : null;
    }

    @Override
    public @Nullable String itemId(ItemStack itemStack) {
        return OraxenItems.getIdByItem(itemStack);
    }

    @Override
    public CompletableFuture<Void> initialized() {
        return initializedFuture;
    }

    @Override
    public boolean enabled() {
        return ENABLED && Bukkit.getPluginManager().isPluginEnabled("Oraxen");
    }

    @Override
    public String getId() {
        return "oraxen";
    }

    @Override
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
        this.initializedFuture = new CompletableFuture<>();
    }

    @EventHandler
    public void onOraxenItemsLoaded(OraxenItemsLoadedEvent event) {
        initializedFuture.complete(null);
    }

}
