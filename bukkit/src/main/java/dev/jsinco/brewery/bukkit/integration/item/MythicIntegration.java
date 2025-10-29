package dev.jsinco.brewery.bukkit.integration.item;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythiccrucible.events.MythicCrucibleLoadedEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MythicIntegration implements ItemIntegration, Listener {
    CompletableFuture<Void> initialized = new CompletableFuture<>();

    @Override
    public Optional<ItemStack> createItem(String id) {
        return getMythicItem(id)
                .map(item -> item.generateItemStack(1))
                .map(stack -> (BukkitItemStack) stack)
                .map(BukkitItemStack::getItemStack);
    }

    @Override
    public boolean isIngredient(String id) {
        return getMythicItem(id).isPresent();
    }

    @Override
    public @Nullable Component displayName(String id) {
        return createItem(id)
                .map(ItemStack::displayName)
                .orElse(null);
    }

    private Optional<MythicItem> getMythicItem(String name) {
        Optional<MythicItem> result = MythicBukkit.inst().getItemManager().getItem(name);
        if (result.isPresent()) return result;
        return MythicBukkit.inst().getItemManager().getItems().stream()
                .filter(item -> item.getInternalName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public @Nullable String getItemId(ItemStack itemStack) {
        return MythicBukkit.inst().getItemManager().getMythicTypeFromItem(itemStack);
    }

    @Override
    public CompletableFuture<Void> initialized() {
        return initialized;
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("io.lumine.mythiccrucible.events.MythicCrucibleLoadedEvent");
    }

    @Override
    public String getId() {
        return "mythic";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
        this.initialized = new CompletableFuture<>();
    }

    @EventHandler
    public void onMythicReload(MythicCrucibleLoadedEvent event) {
        initialized.completeAsync(() -> null);
    }
}
