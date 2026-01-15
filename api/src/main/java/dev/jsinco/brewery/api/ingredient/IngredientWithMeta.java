package dev.jsinco.brewery.api.ingredient;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record IngredientWithMeta(Ingredient ingredient,
                                 Map<IngredientMeta<?>, Object> meta) implements Ingredient {

    public IngredientWithMeta {
        for (Map.Entry<IngredientMeta<?>, Object> entry : meta.entrySet()) {
            Preconditions.checkArgument(entry.getKey().serializer().appliesTo(entry.getValue()), "Invalid meta ingredient data '" + entry.getKey().key().minimalized(), "' for: " + entry.getValue());
        }
    }

    @Override
    public @NotNull String getKey() {
        return ingredient.getKey();
    }

    @Override
    public @NotNull Component displayName() {
        Component override = get(IngredientMeta.DISPLAY_NAME);
        if (override == null) {
            return ingredient.displayName();
        }
        return override;
    }

    @Override
    public Optional<? extends Ingredient> findMatch(Set<BaseIngredient> baseIngredientSet) {
        return ingredient.findMatch(baseIngredientSet)
                .map(this::applyTo);
    }

    @Override
    public BaseIngredient toBaseIngredient() {
        return null;
    }

    /**
     * Wrap this ingredient with an ingredient with meta instance
     * @param ingredient Ingredient to wrap
     * @return Ingredient with meta wrapping the ingredient
     */
    public IngredientWithMeta applyTo(Ingredient ingredient) {
        return new IngredientWithMeta(ingredient, meta);
    }

    /**
     * @param metaKey ingredientMetaKey
     * @return Ingredient meta value, or null if not present
     * @param <T> Ingredient meta value
     */
    public <T> @Nullable T get(IngredientMeta<T> metaKey) {
        return (T) meta.get(metaKey);
    }
}
