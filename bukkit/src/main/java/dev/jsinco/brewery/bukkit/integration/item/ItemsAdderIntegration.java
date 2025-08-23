package dev.jsinco.brewery.bukkit.integration.item;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.integration.ItemIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ItemsAdderIntegration implements ItemIntegration, Listener {

    private static final boolean ENABLED = ClassUtil.exists("dev.lone.itemsadder.api.CustomStack");
    private CompletableFuture<Void> initializedFuture;

    @Override
    public Optional<ItemStack> createItem(String id) {
        return Optional.of(CustomStack.getInstance(id))
                .map(CustomStack::getItemStack);
    }

    public @Nullable Component displayName(String itemsAdderId) {
        CustomStack customStack = CustomStack.getInstance(itemsAdderId);
        return customStack == null ? null : customStack.getItemStack().effectiveName();
    }

    @Override
    public @Nullable String itemId(ItemStack itemStack) {
        CustomStack customStack = CustomStack.byItemStack(itemStack);
        return customStack == null ? null : customStack.getId();
    }

    @Override
    public CompletableFuture<Void> initialized() {
        return initializedFuture;
    }

    @Override
    public boolean enabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "itemsadder";
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
        this.initializedFuture = new CompletableFuture<>();
    }

    @EventHandler
    public void onItemsAdderItemsLoad(ItemsAdderLoadDataEvent loadDataEvent) {
        initializedFuture.completeAsync(() -> null);
    }
}
