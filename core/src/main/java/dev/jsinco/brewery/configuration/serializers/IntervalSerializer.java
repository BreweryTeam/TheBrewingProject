package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.moment.Interval;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class IntervalSerializer implements TypeSerializer<Interval> {
    @Override
    public Interval deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String aString = node.getString();
        return aString == null ? null : Interval.parseString(aString);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable Interval obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(obj.asString());
    }
}
