package dev.jsinco.brewery.bukkit.integration.chest_shop;

import com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientMeta;
import dev.jsinco.brewery.api.ingredient.IngredientWithMeta;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.ChestShopIntegration;
import dev.jsinco.brewery.bukkit.ingredient.BreweryIngredient;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.util.ClassUtil;
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
        event.matches(matches(event.original(), event.comparison()));
    }

    private boolean matches(ItemStack originalItem, ItemStack comparisonItem) {
        Ingredient original = BukkitIngredientManager.INSTANCE.getIngredient(originalItem);
        double originalScore = 1D;
        if (original instanceof IngredientWithMeta ingredientWithMeta && ingredientWithMeta.get(IngredientMeta.SCORE) instanceof Double score) {
            originalScore = score;
            original = ingredientWithMeta.derivatives().getFirst();
        }
        if (!(original instanceof BreweryIngredient origininalBreweryIngredient)) {
            return false;
        }
        Ingredient comparison = BukkitIngredientManager.INSTANCE.getIngredient(comparisonItem);
        double comparisonScore = 1D;
        if (comparison instanceof IngredientWithMeta ingredientWithMeta && ingredientWithMeta.get(IngredientMeta.SCORE) instanceof Double score) {
            comparisonScore = score;
            comparison = ingredientWithMeta.derivatives().getFirst();
        }
        if (!(comparison instanceof BreweryIngredient comparisonBreweryIngredient)) {
            return false;
        }
        return origininalBreweryIngredient.equals(comparisonBreweryIngredient) && comparisonScore > originalScore;
    }
}
