package dev.jsinco.brewery.bukkit.integration.item;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.integration.ItemIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CraftEngineIntegration implements ItemIntegration, Listener {

    private static final boolean ENABLED = ClassUtil.exists("net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine");
    private CompletableFuture<Void> initializedFuture;

    public @Nullable String itemId(ItemStack itemStack) {
        Item<ItemStack> customItem = BukkitCraftEngine.instance().itemManager().wrap(itemStack);
        return customItem.customId()
                .map(Key::toString)
                .orElse(null);
    }

    @Override
    public CompletableFuture<Void> initialized() {
        return initializedFuture;
    }

    @Override
    public Optional<ItemStack> createItem(String id) {
        return Optional.ofNullable(BukkitCraftEngine.instance().itemManager().createWrappedItem(Key.from(id), null))
                .map(Item::getItem);
    }

    public @Nullable Component displayName(String id) {
        return BukkitCraftEngine.instance().itemManager().buildItemStack(Key.from(id), null).effectiveName();
    }

    @Override
    public boolean enabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "craftengine";
    }

    @Override
    public void initialize() {
        this.initializedFuture = new CompletableFuture<>();
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
    }

    @EventHandler
    public void onCraftEngineReload(CraftEngineReloadEvent ignored) {
        initializedFuture.completeAsync(() -> null);
    }
}
