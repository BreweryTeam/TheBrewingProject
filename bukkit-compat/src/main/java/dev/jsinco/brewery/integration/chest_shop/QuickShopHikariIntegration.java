package dev.jsinco.brewery.integration.chest_shop;

import com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent;
import dev.jsinco.brewery.api.brew.BrewManager;
import dev.jsinco.brewery.api.util.ClassUtil;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import dev.jsinco.brewery.bukkit.api.integration.ChestShopIntegration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public record QuickShopHikariIntegration(TheBrewingProjectApi api,
                                         Plugin owner) implements ChestShopIntegration, Listener {
    @Override
    public String getId() {
        return "quick_shop_hikari";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, owner);
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent");
    }

    @EventHandler(ignoreCancelled = true)
    public void onShopItemMatch(ShopItemMatchEvent event) {
        if (api.getBrewManager().brewName(event.original()).isPresent()) {
            event.matches(matches(event.original(), event.comparison()));
        }
    }

    private boolean matches(ItemStack originalItem, ItemStack comparisonItem) {
        BrewManager<ItemStack> brewManager = api.getBrewManager();
        String original = brewManager.brewName(originalItem)
                .orElseThrow();
        double originalScore = brewManager.brewScore(originalItem)
                .orElse(1D);
        String comparison = brewManager.brewName(comparisonItem)
                .orElse(null);
        double comparisonScore = brewManager.brewScore(comparisonItem)
                .orElse(1D);
        return original.equals(comparison) && comparisonScore > originalScore;
    }
}
