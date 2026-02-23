package dev.jsinco.brewery.bukkit.ingredient;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.ingredient.BaseIngredient;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientMeta;
import dev.jsinco.brewery.api.ingredient.IngredientWithMeta;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.configuration.IngredientsSection;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record BreweryIngredient(BreweryKey ingredientKey) implements BaseIngredient {

    @Override
    public @NotNull String getKey() {
        return ingredientKey.toString();
    }

    @Override
    public @NotNull Component displayName() {
        return TheBrewingProject.getInstance().getRecipeRegistry().getRecipe(ingredientKey.minimalized())
                .map(recipe -> recipe.getRecipeResult(BrewQuality.EXCELLENT))
                .map(RecipeResult::displayName)
                .orElseGet(() -> Component.text(ingredientKey.minimalized()));
    }

    public static Optional<Ingredient> from(ItemStack itemStack) {
        PersistentDataContainerView dataContainer = itemStack.getPersistentDataContainer();
        String key = dataContainer.get(BrewAdapter.BREWERY_TAG, PersistentDataType.STRING);
        if (key == null) {
            return Optional.empty();
        }
        BreweryKey breweryKey = BreweryKey.parse(key);
        return Optional.of(new BreweryIngredient(breweryKey));
    }

    public static Optional<CompletableFuture<Optional<Ingredient>>> from(BreweryKey id) {
        if (!id.namespace().equals("brewery") && !id.namespace().equals("#brewery")) {
            return Optional.empty();
        }
        if (id.namespace().startsWith("#")) {
            return Optional.of(IngredientsSection.ingredients()
                    .getIngredient(id));
        }
        return Optional.<Ingredient>of(new BreweryIngredient(id))
                .map(Optional::of)
                .map(CompletableFuture::completedFuture);
    }
}
