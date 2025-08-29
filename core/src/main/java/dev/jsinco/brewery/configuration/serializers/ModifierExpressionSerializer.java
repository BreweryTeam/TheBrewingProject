package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class ModifierExpressionSerializer implements ObjectSerializer<ModifierExpression> {
    @Override
    public boolean supports(@NonNull Class<? super ModifierExpression> type) {
        return ModifierExpression.class == type;
    }

    @Override
    public void serialize(@NonNull ModifierExpression object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(object.function());
    }

    @Override
    public ModifierExpression deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String expression = data.getValue(String.class);
        return expression == null ? null : new ModifierExpression(expression);
    }
}
