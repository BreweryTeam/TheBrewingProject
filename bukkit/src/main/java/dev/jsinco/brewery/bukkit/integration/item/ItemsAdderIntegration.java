package dev.jsinco.brewery.bukkit.integration.item;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
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

public class ItemsAdderIntegration implements ItemIntegration, Listener {

    private static final boolean ENABLED = ClassUtil.exists("dev.lone.itemsadder.api.CustomStack");
    private CompletableFuture<Void> initializedFuture;

    @Override
    public Optional<ItemStack> createItem(String id) {
        return Optional.ofNullable(CustomStack.getInstance(id))
                .map(CustomStack::getItemStack);
    }

    @Override
    public boolean isIngredient(String id) {
        return CustomStack.getInstance(id) != null;
    }

    public @Nullable Component displayName(String itemsAdderId) {
        CustomStack customStack = CustomStack.getInstance(itemsAdderId);
        return customStack == null ? null : customStack.getItemStack().effectiveName();
    }

    @Override
    public @Nullable String getItemId(ItemStack itemStack) {
        CustomStack customStack = CustomStack.byItemStack(itemStack);
        return customStack == null ? null : customStack.getId();
    }

    @Override
    public CompletableFuture<Void> initialized() {
        return initializedFuture;
    }

    @Override
    public boolean isEnabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "itemsadder";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
        this.initializedFuture = new CompletableFuture<>();
    }

    @Override
    public CompletableFuture<Optional<Ingredient>> createIngredient(String id) {
        if (id.contains(":")) {
            id = id.split(":", 2)[1];
        }
        return ItemIntegration.super.createIngredient(id);
    }

    @EventHandler
    public void onItemsAdderItemsLoad(ItemsAdderLoadDataEvent loadDataEvent) {
        initializedFuture.completeAsync(() -> null);
    }
}
