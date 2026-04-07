package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.api.effect.ModifierConsume;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

public class ModifierConsumePdcType implements PersistentDataType<String, ModifierConsume> {

    public static final ListPersistentDataType<String, ModifierConsume> LIST_INSTANCE = ListPersistentDataType.LIST.listTypeFrom(new ModifierConsumePdcType());

    @Override
    public @NonNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NonNull Class<ModifierConsume> getComplexType() {
        return ModifierConsume.class;
    }

    @Override
    public @NonNull String toPrimitive(@NonNull ModifierConsume complex, @NonNull PersistentDataAdapterContext context) {
        return complex.modifier().name() + ";" + complex.value();
    }

    @Override
    public @NonNull ModifierConsume fromPrimitive(@NonNull String primitive, @NonNull PersistentDataAdapterContext context) {
        String[] split = primitive.split(";");
        String modifierName = split[0];
        double value = Double.parseDouble(split[1]);
        return DrunkenModifierSection.modifiers().optionalModifier(modifierName)
                .map(modifier -> new ModifierConsume(modifier, value, true))
                .orElse(null);
    }
}
