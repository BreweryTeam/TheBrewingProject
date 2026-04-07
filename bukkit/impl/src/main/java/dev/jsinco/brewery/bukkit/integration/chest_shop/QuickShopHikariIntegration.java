package dev.jsinco.brewery.bukkit.integration.chest_shop;

import com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.ChestShopIntegration;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.util.ClassUtil;
import dev.jsinco.brewery.util.IngredientUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class QuickShopHikariIntegration implements ChestShopIntegration, Listener {
    @Override
    public String getId() {
        return "quick_shop_hikari";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent");
    }

    @EventHandler(ignoreCancelled = true)
    public void onShopItemMatch(ShopItemMatchEvent event) {
        if (BrewAdapter.isBrew(event.original())) {
            event.matches(matches(event.original(), event.comparison()));
        }
    }

    private boolean matches(ItemStack originalItem, ItemStack comparisonItem) {
        Ingredient original = BukkitIngredientManager.INSTANCE.getIngredient(originalItem);
        double originalScore = IngredientUtil.score(original)
                .orElse(1D);
        Ingredient comparison = BukkitIngredientManager.INSTANCE.getIngredient(comparisonItem);
        double comparisonScore = IngredientUtil.score(comparison)
                .orElse(1D);
        return original.toBaseIngredient().equals(original.toBaseIngredient()) && comparisonScore > originalScore;
    }
}
