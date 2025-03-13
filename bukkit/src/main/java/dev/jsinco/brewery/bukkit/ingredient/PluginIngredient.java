package dev.jsinco.brewery.bukkit.ingredient;

import dev.jsinco.brewery.recipes.ingredient.Ingredient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

// TODO: cleanup a little bit

/**
 * An ingredient based off of an external plugin ItemStack or local custom item/ingredient
 * defined in 'ingredients.yml'.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class PluginIngredient implements Ingredient<ItemStack> {

    private static final Map<String, Supplier<PluginIngredient>> supportedPlugins = new HashMap<>();

    private String plugin;
    private String itemId;


    @Nullable
    public abstract String getItemIdByItemStack(ItemStack itemStack);


    public static void registerPluginIngredient(String plugin, Supplier<PluginIngredient> supplier) {
        supportedPlugins.put(plugin.toUpperCase(), supplier);
    }

    /**
     * Attempt to get a PluginIngredient from an ItemStack.
     *
     * @param itemStack The ItemStack to get the PluginIngredient from.
     * @return The PluginIngredient if the ItemStack is a valid PluginIngredient, otherwise null.
     */
    public static PluginIngredient of(@NotNull ItemStack itemStack) {
        for (Map.Entry<String, Supplier<PluginIngredient>> entry : supportedPlugins.entrySet()) {
            // This may be inefficient...
            // We're creating a new instance of a PluginIngredient just to check if the ingredient exists
            PluginIngredient pluginIngredient = entry.getValue().get();
            String itemId = pluginIngredient.getItemIdByItemStack(itemStack);
            if (itemId != null) {
                pluginIngredient.plugin = entry.getKey();
                pluginIngredient.itemId = itemId;
                return pluginIngredient;
            }
        }
        return null;
    }

    /**
     * Attempt to get a PluginIngredient from a plugin name and item id.
     *
     * @param plugin The plugin name.
     * @param itemId The item id.
     * @return The PluginIngredient if the plugin is supported, otherwise null.
     */
    public static Optional<PluginIngredient> of(String plugin, String itemId) {
        Supplier<PluginIngredient> supplier = supportedPlugins.get(plugin.toUpperCase());
        if (supplier != null) {
            PluginIngredient pluginIngredient = supplier.get();
            pluginIngredient.plugin = plugin;
            pluginIngredient.itemId = itemId;
            return Optional.of(pluginIngredient);
        }
        return Optional.empty();
    }

    @Override
    public String getKey() {
        return plugin + ":" + itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginIngredient that = (PluginIngredient) o;
        return Objects.equals(plugin, that.plugin) && Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plugin, itemId);
    }

    @Override
    public String toString() {
        return "PluginIngredient(" + getKey() + ")";
    }
}
