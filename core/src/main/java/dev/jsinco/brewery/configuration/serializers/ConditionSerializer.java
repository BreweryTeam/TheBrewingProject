package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.event.step.Condition;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Set;

public class ConditionSerializer implements ObjectSerializer<Condition> {
    @Override
    public boolean supports(@NonNull Class<? super Condition> type) {
        return Condition.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull Condition object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        switch (object) {
            case Condition.Died died -> data.setValue("died");
            case Condition.HasPermission hasPermission -> data.add("has-permission", hasPermission.permission());
            case Condition.JoinedServer joinedServer -> data.setValue("joined-server");
            case Condition.JoinedWorld joinedWorld -> data.add("joined-world", joinedWorld.worldName());
            case Condition.TookDamage tookDamage -> data.setValue("took-damage");
            case Condition.ModifierAbove modifierAbove -> {
                data.add("modifier-above", Map.of(
                        "modifier", modifierAbove.modifier(),
                        "value", modifierAbove.value())
                );
            }
            case Condition.NotCondition notCondition -> data.add("not", notCondition.toInvert());
        }
    }

    @Override
    public Condition deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        if (data.isValue()) {
            return switch (data.getValue(String.class)) {
                case "died" -> new Condition.Died();
                case "joined-server", "join" -> new Condition.JoinedServer();
                case "took-damage" -> new Condition.TookDamage();
                default -> throw new IllegalArgumentException("Unknown data value: " + data.getValue(String.class));
            };
        }
        Set<String> keys = data.asMap().keySet();
        Preconditions.checkArgument(!keys.isEmpty(), "Condition can not be empty");
        Preconditions.checkArgument(keys.size() == 1, "Condition can not be built from multiple condition types, found: " + keys);
        if (data.containsKey("has-permission")) {
            return new Condition.HasPermission(data.get("has-permission", String.class));
        }
        if (data.containsKey("joined-world")) {
            return new Condition.JoinedWorld(data.get("joined-world", String.class));
        }
        if (data.containsKey("modifier-above")) {
            Map<String, Object> modifierAbove = data.getAsMap("modifier-above", String.class, Object.class);
            Preconditions.checkArgument(modifierAbove.get("modifier") instanceof String, "Expected a modifier string definition");
            Preconditions.checkArgument(modifierAbove.get("value") instanceof Number, "Expected a value number definition");
            String modifierName = modifierAbove.get("modifier").toString();
            Preconditions.checkArgument(DrunkenModifierSection.modifiers().optionalModifier(modifierName).isPresent(), "Unknown modifier: " + modifierName);
            return new Condition.ModifierAbove(modifierName, ((Number) modifierAbove.get("value")).doubleValue());
        }
        if (data.containsKey("not")) {
            return new Condition.NotCondition(data.get("not", Condition.class));
        }
        throw new IllegalArgumentException("Could not serialize from key: " + keys.stream().findAny().get());
    }
}
