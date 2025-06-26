package dev.jsinco.brewery.bukkit.ingredient;

import dev.jsinco.brewery.bukkit.integration.ItemIntegration;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.util.BreweryKey;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PluginIngredient implements Ingredient {
    private final ItemIntegration itemIntegration;
    private final BreweryKey key;

    public PluginIngredient(BreweryKey key, ItemIntegration itemIntegration) {
        this.key = key;
        this.itemIntegration = itemIntegration;
    }

    @Override
    public @NotNull String getKey() {
        return key.toString();
    }

    @Override
    public @NotNull Component displayName() {
        Component displayName = itemIntegration.displayName(key.key());
        return displayName == null ? Component.text(key.key()) : displayName;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PluginIngredient that = (PluginIngredient) o;
        return Objects.equals(key, that.key) && itemIntegration == that.itemIntegration;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}
