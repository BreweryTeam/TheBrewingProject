package dev.jsinco.brewery.bukkit.integration.item;

import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.integration.ItemIntegration;
import dev.jsinco.brewery.util.ClassUtil;
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

public class CraftEngineHook implements ItemIntegration, Listener {

    private static final boolean ENABLED = ClassUtil.exists("net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine");
    private CompletableFuture<Void> initializedFuture;

    public @Nullable String itemId(ItemStack itemStack) {
        return BukkitCraftEngine.instance().itemManager().itemId(itemStack).toString();
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

    public @Nullable String displayName(String id) {
        return BukkitCraftEngine.instance().itemManager().createWrappedItem(Key.from(id), null).customName().orElse(id);
    }

    @Override
    public boolean enabled() {
        return ENABLED && Bukkit.getPluginManager().isPluginEnabled("CraftEngine");
    }

    @Override
    public String getId() {
        return "craftengine";
    }

    @Override
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
        this.initializedFuture = new CompletableFuture<>();
    }

    @EventHandler
    public void onItemsLoaded(NexoItemsLoadedEvent itemsLoadedEvent) {
        initializedFuture.complete(null);
    }
}
