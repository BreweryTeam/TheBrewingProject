package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.WildcardIngredient;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.bukkit.ingredient.UncheckedIngredientImpl;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.api.ingredient.UncheckedIngredient;
import dev.jsinco.brewery.util.ItemColorUtil;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BukkitIngredientUtil {

    public static Pair<org.bukkit.Color, @Nullable Ingredient> ingredientData(Map<? extends Ingredient, Integer> ingredients) {
        int r = 0;
        int g = 0;
        int b = 0;
        int amount = 0;
        Ingredient topIngredient = null;
        int topIngredientAmount = 0;
        for (Map.Entry<? extends Ingredient, Integer> ingredient : ingredients.entrySet()) {
            if (topIngredientAmount < ingredient.getValue()) {
                topIngredient = ingredient.getKey();
                topIngredientAmount = ingredient.getValue();
            }
            String key = ingredient.getKey().getKey();
            Color color = ItemColorUtil.getItemColor(key);
            if (color == null) {
                continue;
            }
            r += color.getRed() * ingredient.getValue();
            g += color.getGreen() * ingredient.getValue();
            b += color.getBlue() * ingredient.getValue();
            amount += ingredient.getValue();
        }
        if (amount != 0) {
            return new Pair<>(org.bukkit.Color.fromRGB(r / amount, g / amount, b / amount), topIngredient);
        } else {
            return new Pair<>(org.bukkit.Color.YELLOW, topIngredient);
        }
    }

    public static @Nullable List<String> tagValuesFromString(String key) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(key);
        if (namespacedKey == null) {
            return null;
        }
        Tag<? extends Keyed> itemTag = Bukkit.getTag(Tag.REGISTRY_ITEMS, namespacedKey, Material.class);
        if (itemTag == null) {
            return null;
        }
        return itemTag.getValues()
                .stream().map(Keyed::key)
                .map(Key::asMinimalString)
                .toList();
    }

    public static boolean isValidTag(String key) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(key);
        if (namespacedKey == null) {
            return false;
        }
        return Bukkit.getTag(Tag.REGISTRY_ITEMS, namespacedKey, Material.class) != null;
    }


    public static Optional<ItemStack> computeTransform(ItemStack ingredientItem) {
        UncheckedIngredientImpl ingredient = new UncheckedIngredientImpl(BukkitIngredientManager.INSTANCE.getIngredient(ingredientItem));
        UncheckedIngredient ingredientTransform = Config.config().cauldrons().ingredientEmptyTransforms()
                .get(ingredient);
        if (ingredientTransform != null) {
            return ingredientTransform.retrieve()
                    .flatMap(BukkitIngredientManager.INSTANCE::toItem);
        }
        ingredientTransform = Config.config().cauldrons().ingredientEmptyTransforms().entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches(ingredient))
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(null);
        if (ingredientTransform != null) {
            return ingredientTransform.retrieve()
                    .flatMap(BukkitIngredientManager.INSTANCE::toItem);
        }
        return Optional.empty();
    }
}
