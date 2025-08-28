package dev.jsinco.brewery.bukkit.recipe;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.moment.Interval;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class RecipeEffectPersistentDataType implements PersistentDataType<String, RecipeEffect> {

    private static final RecipeEffectPersistentDataType SINGLETON_INSTANCE = new RecipeEffectPersistentDataType();
    public static final ListPersistentDataType<String, RecipeEffect> INSTANCE = ListPersistentDataType.LIST.listTypeFrom(SINGLETON_INSTANCE);

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<RecipeEffect> getComplexType() {
        return RecipeEffect.class;
    }

    @Override
    public @NotNull String toPrimitive(@NotNull RecipeEffect complex, @NotNull PersistentDataAdapterContext context) {
        return complex.type().key().asString() + "/" + complex.durationRange().asString() + "/" + complex.amplifierRange().asString();
    }

    @Override
    public @NotNull RecipeEffect fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        String[] split = primitive.split("/");
        PotionEffectType effectType = Registry.EFFECT.get(NamespacedKey.fromString(split[0]));
        Preconditions.checkArgument(effectType != null, "Effect type can not be null");
        Interval duration = Interval.parseString(split[1]);
        Interval amplifier = Interval.parseString(split[2]);
        return new RecipeEffect(effectType, duration, amplifier);
    }
}
