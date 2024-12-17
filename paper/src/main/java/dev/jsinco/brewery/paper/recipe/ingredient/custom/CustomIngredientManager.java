package dev.jsinco.brewery.paper.recipe.ingredient.custom;

import dev.jsinco.brewery.paper.TheBrewingProject;
import dev.jsinco.brewery.paper.config.sections.CustomIngredientsConfig;
import dev.jsinco.brewery.paper.recipe.ingredient.PluginIngredient;
import dev.jsinco.brewery.paper.util.Util;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomIngredientManager extends PluginIngredient {

    // FIXME: I don't feel good about this implementation. It feels slow. Everytime we're getting an ingredient ItemStack we're looping through every CustomItem - Jsinco
    private static final List<CustomIngredient> customIngredients = new ArrayList<>();


    public static void reloadCustomIngredients() {
        CustomIngredientsConfig config = TheBrewingProject.getInstance().getConfigManager().getCustomIngredients();

        if (!customIngredients.isEmpty()) {
            customIngredients.clear();
        }

        for (var entry : config.getCustomIngredients().entrySet()) {
            CustomIngredient customIngredient = new CustomIngredient(entry.getKey(),
                    entry.getValue().getName(),
                    entry.getValue().getLore(),
                    Util.getEnumByName(Material.class, entry.getValue().getMaterial()),
                    -1);
            customIngredients.add(customIngredient);
        }
    }

    @Override
    public String getItemIdByItemStack(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }

        for (CustomIngredient customIngredient : customIngredients) {
            if (itemMeta.hasDisplayName() && !itemMeta.getDisplayName().equals(customIngredient.name())) {
                continue;
            } else if (itemMeta.hasLore() && !itemMeta.getLore().equals(customIngredient.lore())) {
                continue;
            } else if (itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() != customIngredient.customModelData()) {
                continue;
            } else if (itemMeta.getCustomModelData() != customIngredient.customModelData()) {
                continue;
            }
            return customIngredient.id();
        }
        return null;
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        return this.getItemIdByItemStack(itemStack) != null;
    }
}
