package dev.jsinco.brewery.bukkit.integration.item;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import com.nexomc.nexo.items.ItemBuilder;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NexoIntegration implements ItemIntegration, Listener {

    private static final boolean ENABLED = ClassUtil.exists("com.nexomc.nexo.api.NexoItems");
    private CompletableFuture<Void> initializedFuture;

    @Override
    public Optional<ItemStack> createItem(String id) {
        ItemBuilder itemBuilder = NexoItems.itemFromId(id);
        if (itemBuilder == null) {
            return Optional.empty();
        }
        return Optional.of(itemBuilder.build());
    }

    public @Nullable Component displayName(String itemsAdderId) {
        if (!ENABLED) {
            return null;
        }
        ItemBuilder itemBuilder = NexoItems.itemFromId(itemsAdderId);
        if (itemBuilder == null) {
            return null;
        }
        return itemBuilder.getDisplayName();
    }

    @Override
    public @Nullable String getItemId(ItemStack itemStack) {
        return NexoItems.idFromItem(itemStack);
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
        return "nexo";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
        this.initializedFuture = new CompletableFuture<>();
    }

    @EventHandler
    public void onNexoItemsLoaded(NexoItemsLoadedEvent event) {
        initializedFuture.completeAsync(() -> null);
    }
}
