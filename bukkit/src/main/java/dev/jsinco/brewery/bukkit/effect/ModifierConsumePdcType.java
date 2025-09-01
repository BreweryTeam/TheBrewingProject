package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.api.effect.ModifierConsume;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ModifierConsumePdcType implements PersistentDataType<String, ModifierConsume> {

    public static final ListPersistentDataType<String, ModifierConsume> LIST_INSTANCE = ListPersistentDataType.LIST.listTypeFrom(new ModifierConsumePdcType());

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<ModifierConsume> getComplexType() {
        return ModifierConsume.class;
    }

    @Override
    public @NotNull String toPrimitive(@NotNull ModifierConsume complex, @NotNull PersistentDataAdapterContext context) {
        return complex.modifier().name() + ";" + complex.value();
    }

    @Override
    public @NotNull ModifierConsume fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        String[] split = primitive.split(";");
        String modifier = split[0];
        double value = Double.parseDouble(split[1]);
        return new ModifierConsume(DrunkenModifierSection.modifiers().modifier(modifier), value);
    }
}
