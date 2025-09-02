package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.effect.modifier.ModifierDisplay;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class ModifierDisplaySerializer implements ObjectSerializer<ModifierDisplay> {
    @Override
    public boolean supports(@NonNull Class<? super ModifierDisplay> type) {
        return ModifierDisplay.class == type;
    }

    @Override
    public void serialize(@NonNull ModifierDisplay object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("message", object.message());
        data.add("filter", object.filter());
        data.add("display-value", object.value());
        data.add("display-window", object.displayWindow());
    }

    @Override
    public ModifierDisplay deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String message = data.get("message", String.class);
        ModifierExpression filter = data.get("filter", ModifierExpression.class);
        ModifierExpression value = data.get("display-value", ModifierExpression.class);
        ModifierDisplay.DisplayWindow window = data.get("display-window", ModifierDisplay.DisplayWindow.class);
        Preconditions.checkArgument(filter != null, "Modifier display requires a filter");
        Preconditions.checkArgument(value != null, "Modifier display requires a display-value");
        Preconditions.checkArgument(message != null, "Modifier display requires a message");
        return new ModifierDisplay(message, filter, value, window == null ? ModifierDisplay.DisplayWindow.ACTION_BAR : window);
    }
}
