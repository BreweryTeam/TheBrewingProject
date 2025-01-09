package dev.jsinco.brewery.recipes.ingredient;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a simple ingredient that only consists of a material and an amount
 */
public class SimpleIngredient implements Ingredient {

    private final Material material;

    public SimpleIngredient(Material material) {
        this.material = material;
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        return itemStack.getType() == material;
    }

    @Override
    public NamespacedKey getKey() {
        return material.getKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleIngredient that = (SimpleIngredient) o;
        return material == that.material;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(material);
    }

    public static SimpleIngredient of(@NotNull ItemStack itemStack) {
        return new SimpleIngredient(itemStack.getType());
    }

    /**
     * Returns an optional with a {@link SimpleIngredient} if the material string could be parsed.
     * Initially tries to read the ingredient within the minecraft namespace, if it can't find any
     * match there, it tries to read it directly from the {@link Material} enum.
     *
     * @param materialStr A string representing the material
     * @return An optional simple ingredient
     */
    public static Optional<SimpleIngredient> of(String materialStr) {
        return Optional.ofNullable(NamespacedKey.fromString(materialStr.toLowerCase(Locale.ROOT)))
                .flatMap(namespacedKey -> Optional.ofNullable(Registry.MATERIAL.get(namespacedKey)))
                .map(SimpleIngredient::new);
    }
}
