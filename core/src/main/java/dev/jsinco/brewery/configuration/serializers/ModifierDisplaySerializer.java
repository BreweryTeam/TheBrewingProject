package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.effect.modifier.ModifierDisplay;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

public class ModifierDisplaySerializer implements ObjectSerializer<ModifierDisplay> {
    @Override
    public boolean supports(@NonNull Class<? super ModifierDisplay> type) {
        return ModifierDisplay.class == type;
    }

    @Override
    public void serialize(@NonNull ModifierDisplay object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("display-name", object.displayName());
        data.add("expression", object.expression());
        data.add("type", object.type());
        data.add("display-window", object.displayWindow());
    }

    @Override
    public ModifierDisplay deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        Component displayName = data.get("display-name", Component.class);
        ModifierExpression expression = data.get("expression", ModifierExpression.class);
        ModifierDisplay.DisplayType type = data.get("type", ModifierDisplay.DisplayType.class);
        ModifierDisplay.DisplayWindow window = data.get("display-window", ModifierDisplay.DisplayWindow.class);
        Preconditions.checkArgument(expression != null, "Modifier display requires an expression");
        Preconditions.checkArgument(displayName != null, "Modifier display requires a display name");
        return new ModifierDisplay(displayName, expression, type == null ? ModifierDisplay.DisplayType.BARS : type, window == null ? ModifierDisplay.DisplayWindow.BAR : window);
    }
}
