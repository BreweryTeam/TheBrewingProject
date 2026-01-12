package dev.jsinco.brewery.api.ingredient;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record IngredientWithMeta(Ingredient ingredient,
                                 Map<IngredientMeta<?>, Object> meta) implements ComplexIngredient {

    public IngredientWithMeta {
        for (Map.Entry<IngredientMeta<?>, Object> entry : meta.entrySet()) {
            Preconditions.checkArgument(entry.getKey().serializer().appliesTo(entry.getValue()), "Invalid meta ingredient data '" + entry.getKey().key().minimalized(), "' for: " + entry.getValue());
        }
        Preconditions.checkArgument(ingredient instanceof BaseIngredient || ingredient instanceof ComplexIngredient, "Ingredient has to extend complex or base ingredient");
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

    public <T> @Nullable T get(IngredientMeta<T> tKey) {
        return (T) meta.get(tKey);
    }

    @Override
    public List<BaseIngredient> derivatives() {
       if(ingredient instanceof BaseIngredient baseIngredient) {
           return List.of(baseIngredient);
       }
       if(ingredient instanceof ComplexIngredient complexIngredient) {
           return complexIngredient.derivatives();
       }
       throw new IllegalStateException("Unknown ingredient class, has to be base or complex ingredient");
    }
}
