package dev.jsinco.brewery.bukkit.integration.item;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.core.items.MythicItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MythicIntegration implements ItemIntegration, Listener {
    private final CompletableFuture<Void> initialized = new CompletableFuture<>();

    @Override
    public Optional<ItemStack> createItem(String id) {
        return getMythicItem(id)
                .map(item -> item.generateItemStack(1))
                .map(BukkitItemStack.class::cast)
                .map(BukkitItemStack::getItemStack);
    }

    @Override
    public boolean isIngredient(String id) {
        return getMythicItem(id).isPresent();
    }

    @Override
    public @Nullable Component displayName(String id) {
        return createItem(id)
                .map(ItemStack::effectiveName)
                .orElse(null);
    }

    private Optional<MythicItem> getMythicItem(String name) {
        MythicBukkit bukkit = MythicBukkit.inst();
        Optional<MythicItem> result = bukkit.getItemManager().getItem(name);
        if (result.isPresent()) return result;

        return bukkit.getItemManager().getItems().stream()
                .filter(item -> item.getInternalName().equalsIgnoreCase(name))
                .findFirst();

    }

    @Override
    public @Nullable String getItemId(ItemStack itemStack) {
        return MythicBukkit.inst().getItemManager().getMythicTypeFromItem(itemStack);
    }

    @Override
    public @NonNull CompletableFuture<Void> initialized() {
        return initialized;
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("io.lumine.mythic.bukkit.MythicBukkit");
    }

    @Override
    public String getId() {
        return "mythic";
    }

    @Override
    public void onEnable() {
        Bukkit.getGlobalRegionScheduler()
                .run(TheBrewingProject.getInstance(), ignored -> initialized.completeAsync(() -> null));
    }
}
