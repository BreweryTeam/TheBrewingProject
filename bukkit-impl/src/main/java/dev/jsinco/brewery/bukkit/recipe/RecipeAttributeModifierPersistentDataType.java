package dev.jsinco.brewery.bukkit.recipe;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.moment.Interval;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

public class RecipeAttributeModifierPersistentDataType implements PersistentDataType<String, RecipeAttributeModifierImpl> {

    private static final RecipeAttributeModifierPersistentDataType SINGLETON_INSTANCE = new RecipeAttributeModifierPersistentDataType();
    public static final ListPersistentDataType<String, RecipeAttributeModifierImpl> INSTANCE = ListPersistentDataType.LIST.listTypeFrom(SINGLETON_INSTANCE);

    @Override
    public @NonNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NonNull Class<RecipeAttributeModifierImpl> getComplexType() {
        return RecipeAttributeModifierImpl.class;
    }

    @Override
    public @NonNull String toPrimitive(@NonNull RecipeAttributeModifierImpl complex, @NonNull PersistentDataAdapterContext context) {
        return complex.attribute().getKey().asString() + "/" + complex.operation().name() + "/"
                + complex.amountRange().asString() + "/" + complex.durationRange().asString() + "/" + complex.key().asString();
    }

    @Override
    public @NonNull RecipeAttributeModifierImpl fromPrimitive(@NonNull String primitive, @NonNull PersistentDataAdapterContext context) {
        String[] split = primitive.split("/", 5);
        Preconditions.checkArgument(split.length == 5, "Invalid attribute modifier data: " + primitive);
        NamespacedKey key = NamespacedKey.fromString(split[0]);
        Attribute attribute = key == null ? null : Registry.ATTRIBUTE.get(key);
        Preconditions.checkArgument(attribute != null, "Attribute can not be null");
        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(split[1]);
        RecipeAttributeModifierImpl.DoubleRange amount = RecipeAttributeModifierImpl.DoubleRange.parse(split[2], "pdc#read");
        Interval duration = Interval.parseString(split[3]);
        NamespacedKey modifierKey = NamespacedKey.fromString(split[4]);
        Preconditions.checkArgument(modifierKey != null, "Invalid attribute modifier key: " + split[4]);
        return new RecipeAttributeModifierImpl(attribute, operation, amount, duration, modifierKey);
    }
}
