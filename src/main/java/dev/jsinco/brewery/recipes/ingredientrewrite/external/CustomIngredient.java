package dev.jsinco.brewery.recipes.ingredientrewrite.external;

import dev.jsinco.brewery.recipes.ingredientrewrite.PluginIngredient;
import org.bukkit.inventory.ItemStack;

public class CustomIngredient extends PluginIngredient {

    // TODO: handle this tomorrow

    @Override
    public String getItemIdByItemStack(ItemStack itemStack) {
        return "";
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        return false;
    }
}
