package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

public class DrunkenModifierSerializer implements ObjectSerializer<DrunkenModifier> {
    @Override
    public boolean supports(@NonNull Class<? super DrunkenModifier> type) {
        return DrunkenModifier.class == type;
    }

    @Override
    public void serialize(@NonNull DrunkenModifier object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("name", object.name());
        data.add("min-value", object.minValue());
        data.add("max-value", object.maxValue());
        data.add("decrement-time", object.decrementTime());
        data.add("increment-dependency", object.dependency());
        data.add("display-name", object.displayName());
    }

    @Override
    public DrunkenModifier deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String name = data.get("name", String.class);
        Double minValue = data.get("min-value", Double.class);
        Double maxValue = data.get("max-value", Double.class);
        ModifierExpression decrementTime = data.get("decrement-time", ModifierExpression.class);
        ModifierExpression incrementDependency = data.get("increment-dependency", ModifierExpression.class);
        Component displayName = data.get("display-name", Component.class);
        Preconditions.checkArgument(name != null, "Missing drunken modifier name");
        return new DrunkenModifier(
                name,
                incrementDependency == null ? new ModifierExpression("0") : incrementDependency,
                decrementTime == null ? new ModifierExpression("-1") : decrementTime,
                minValue == null ? 0D : minValue,
                maxValue == null ? 0D : maxValue,
                displayName == null ? Component.text(name) : displayName
        );
    }
}
