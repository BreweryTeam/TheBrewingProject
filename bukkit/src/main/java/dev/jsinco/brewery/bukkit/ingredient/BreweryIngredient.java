package dev.jsinco.brewery.bukkit.ingredient;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.ScoredIngredient;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.configuration.IngredientsSection;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BreweryIngredient implements Ingredient {
    protected final BreweryKey ingredientKey;
    private final String displayName;

    public BreweryIngredient(BreweryKey ingredientKey, String displayName) {
        this.ingredientKey = ingredientKey;
        this.displayName = displayName;
    }

    @Override
    public @NotNull String getKey() {
        return ingredientKey.toString();
    }

    @Override
    public @NotNull Component displayName() {
        return MessageUtil.miniMessage(displayName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BreweryIngredient that = (BreweryIngredient) o;
        return Objects.equals(ingredientKey, that.ingredientKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ingredientKey);
    }

    public static Optional<Ingredient> from(ItemStack itemStack) {
        PersistentDataContainerView dataContainer = itemStack.getPersistentDataContainer();
        String key = dataContainer.get(BrewAdapter.BREWERY_TAG, PersistentDataType.STRING);
        if (key == null) {
            return Optional.empty();
        }
        Double score = dataContainer.get(BrewAdapter.BREWERY_SCORE, PersistentDataType.DOUBLE);
        String displayName = dataContainer.get(BrewAdapter.BREWERY_DISPLAY_NAME, PersistentDataType.STRING);
        BreweryKey breweryKey = BreweryKey.parse(key);
        displayName = displayName == null ? breweryKey.key() : displayName;
        if (score != null) {
            return Optional.of(new ScoredIngredient(new BreweryIngredient(breweryKey, displayName), score));
        }
        return Optional.of(new BreweryIngredient(breweryKey, displayName));
    }

    public static Optional<CompletableFuture<Optional<Ingredient>>> from(String id) {
        if (!id.startsWith("brewery:") && !id.startsWith("#brewery:")) {
            return Optional.empty();
        }
        BreweryKey breweryKey = BreweryKey.parse(id);
        if (breweryKey.namespace().startsWith("#")) {
            String ingredientKey = breweryKey.key();
            return IngredientsSection.ingredients().ingredientGroups()
                    .stream()
                    .filter(ingredient -> ingredient.key().equals(ingredientKey))
                    .map(ingredient -> ingredient.create(BukkitIngredientManager.INSTANCE, key -> {
                                NamespacedKey namespacedKey = NamespacedKey.fromString(key);
                                if (namespacedKey == null) {
                                    return null;
                                }
                                Tag<Material> itemTag = Bukkit.getTag(Tag.REGISTRY_ITEMS, namespacedKey, Material.class);
                                if (itemTag == null) {
                                    return null;
                                }
                                return itemTag.getValues()
                                        .stream().map(Keyed::key)
                                        .map(Key::asMinimalString)
                                        .toList();
                            }
                    ))
                    .findFirst();
        }
        return Optional.<Ingredient>of(new BreweryIngredient(breweryKey, breweryKey.key()))
                .map(Optional::of)
                .map(CompletableFuture::completedFuture);
    }
}
