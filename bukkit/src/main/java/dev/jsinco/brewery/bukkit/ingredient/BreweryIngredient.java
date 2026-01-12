package dev.jsinco.brewery.bukkit.ingredient;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.ingredient.BaseIngredient;
import dev.jsinco.brewery.api.ingredient.IngredientMeta;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientWithMeta;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.configuration.IngredientsSection;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BreweryIngredient implements BaseIngredient {
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
        String displayNameString = dataContainer.get(BrewAdapter.BREWERY_DISPLAY_NAME, PersistentDataType.STRING);
        BreweryKey breweryKey = BreweryKey.parse(key);
        Component displayName = displayNameString == null ? Component.text(breweryKey.key()) : MiniMessage.miniMessage().deserialize(displayNameString);
        BaseIngredient baseIngredient = new BreweryIngredient(breweryKey, displayNameString);
        ImmutableMap.Builder<IngredientMeta<?>, Object> extraBuilder = new ImmutableMap.Builder<>();
        extraBuilder.put(IngredientMeta.DISPLAY_NAME_OVERRIDE, displayName);
        if (score != null) {
            extraBuilder.put(IngredientMeta.SCORE, score);
        }
        return Optional.of(new IngredientWithMeta(baseIngredient, extraBuilder.build()));
    }

    public static Optional<CompletableFuture<Optional<Ingredient>>> from(BreweryKey id) {
        if (!id.namespace().equals("brewery") && !id.namespace().equals("#brewery")) {
            return Optional.empty();
        }
        if (id.namespace().startsWith("#")) {
            return Optional.of(IngredientsSection.ingredients()
                    .getIngredient(id));
        }
        return Optional.<Ingredient>of(new BreweryIngredient(id, id.key()))
                .map(Optional::of)
                .map(CompletableFuture::completedFuture);
    }
}
