package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.effect.modifier.ModifierTooltip;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class ModifierTooltipSerializer implements ObjectSerializer<ModifierTooltip> {
    @Override
    public boolean supports(@NonNull Class<? super ModifierTooltip> type) {
        return type == ModifierTooltip.class;
    }

    @Override
    public void serialize(@NonNull ModifierTooltip object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("expression", object.expression());
        data.add("brewing-tooltip", object.brewingTooltip());
        data.add("default-tooltip", object.defaultTooltip());
        data.add("sealed-tooltip", object.sealedTooltip());
    }

    @Override
    public ModifierTooltip deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        ModifierExpression modifierExpression = data.get("expression", ModifierExpression.class);
        String brewingTooltip = data.get("brewing-tooltip", String.class);
        String defaultTooltip = data.get("default-tooltip", String.class);
        String sealedTooltip = data.get("sealed-tooltip", String.class);
        Preconditions.checkArgument(modifierExpression != null, "Tooltip display requires an expression and a tooltip message");
        return new ModifierTooltip(modifierExpression, brewingTooltip, defaultTooltip, sealedTooltip);
    }
}
