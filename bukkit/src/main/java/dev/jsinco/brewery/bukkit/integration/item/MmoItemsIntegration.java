package dev.jsinco.brewery.bukkit.integration.item;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.MMOItemsReloadEvent;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MmoItemsIntegration implements ItemIntegration, Listener {
    CompletableFuture<Void> initialized = new CompletableFuture<>();

    @Override
    public Optional<ItemStack> createItem(String id) {
        return getMmoItem(id)
                .map(MMOItem::newBuilder)
                .map(ItemStackBuilder::build);
    }

    @Override
    public boolean isIngredient(String id) {
        return getMmoItem(id).isPresent();
    }

    @Override
    public @Nullable Component displayName(String id) {
        return createItem(id)
                .map(ItemStack::displayName)
                .orElse(null);
    }

    private Optional<MMOItem> getMmoItem(String id) {
        String[] split = id.split(":");
        Preconditions.checkArgument(split.length == 2, "mmoitems id needs to be in the format <type>:<id>");
        return Optional.ofNullable(MMOItems.plugin.getTemplates().getTemplate(Type.get(split[0]), split[1]))
                .map(MMOItemTemplate::newBuilder)
                .map(MMOItemBuilder::build);
    }

    @Override
    public @Nullable String getItemId(ItemStack itemStack) {
        NBTItem nbtItem = NBTItem.get(itemStack);
        if (!nbtItem.hasType()) {
            return null;
        }
        return nbtItem.getType() + ":" + nbtItem.get("MMOITEMS_ITEM_ID");
    }

    @Override
    public CompletableFuture<Void> initialized() {
        return initialized;
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("net.Indyuce.mmoitems.MMOItems");
    }

    @Override
    public String getId() {
        return "mmoitems";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
        this.initialized = new CompletableFuture<>();
    }

    @EventHandler
    public void onMmoItemsReload(MMOItemsReloadEvent event) {
        initialized.completeAsync(() -> null);
    }
}
