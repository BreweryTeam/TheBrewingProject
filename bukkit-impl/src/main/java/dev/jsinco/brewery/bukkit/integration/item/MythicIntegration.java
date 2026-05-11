package dev.jsinco.brewery.bukkit.integration.item;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import dev.jsinco.brewery.bukkit.util.color.ResourcePackColors;
import dev.jsinco.brewery.util.ClassUtil;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.core.items.MythicItem;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MythicIntegration implements ItemIntegration, Listener {
    private final CompletableFuture<Void> initialized = new CompletableFuture<>();
    private final ResourcePackColors resourcePackColors;

    public MythicIntegration(ResourcePackColors resourcePackColors) {
        this.resourcePackColors = resourcePackColors;
    }

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
        Bukkit.getAsyncScheduler().runAtFixedRate(
                TheBrewingProject.getInstance(),
                this::checkEnabled,
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    private void checkEnabled(ScheduledTask task) {
        io.lumine.mythic.api.items.ItemManager manager = MythicBukkit.inst().getItemManager();
        if (manager == null) {
            return;
        }
        Collection<MythicItem> items = manager.getItems();
        if (items != null && !items.isEmpty()) {
            File resourcePack = new File(MythicBukkit.inst().getDataFolder(), "Generation/resource_pack.zip");
            if (resourcePack.exists() && resourcePack.isFile()) {
                resourcePackColors.addSource(new ResourcePackColors.FileResourcePackSource(resourcePack));
            }
            initialized.complete(null);
            task.cancel();
        }
    }


    @Override
    public @Nullable Color color(String id) {
        return getMythicItem(id)
                .flatMap(item -> Optional.ofNullable(item.getItemModel()))
                .flatMap(key -> Optional.ofNullable(resourcePackColors.modelColor(key)))
                .orElse(null);
    }

}
