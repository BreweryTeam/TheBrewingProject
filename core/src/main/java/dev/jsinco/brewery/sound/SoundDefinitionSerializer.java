package dev.jsinco.brewery.sound;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.math.RangeF;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public class SoundDefinitionSerializer implements TypeSerializer<SoundDefinition> {


    @Override
    public SoundDefinition deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String serialized = node.get(String.class);
        if (serialized.isBlank()) {
            return SoundDefinition.SILENT;
        }
        try {
            return parseSoundDefinition(serialized);
        } catch (IllegalArgumentException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public void serialize(Type type, @Nullable SoundDefinition obj, ConfigurationNode node) throws SerializationException {
        if (obj == null || obj == SoundDefinition.SILENT) {
            node.set("");
            return;
        }
        if (Objects.equals(obj.pitch(), new RangeF(1F, 1F))) {
            node.set(obj.soundKey().toString());
            return;
        }
        if (obj.pitch().min() == obj.pitch().max()) {
            node.set(obj.soundKey().toString() + "/" + obj.pitch());
            return;
        }
        node.set(obj.soundKey().toString() + "/" + obj.pitch().min() + ";" + obj.pitch().max());
    }

    public static SoundDefinition parseSoundDefinition(String string) {
        String[] split = string.split("/");

        if (split.length > 2) {
            throw new IllegalArgumentException("Invalid sound definition");
        }

        Key sound = Key.key(split[0]);
        RangeF pitchRange = split.length > 1 ? RangeF.fromString(split[1]) : new RangeF(1.0f, 1.0f);
        Preconditions.checkArgument(pitchRange.min() >= 0, "Minimum value of range needs to be 0 or larger");
        Preconditions.checkArgument(pitchRange.max() <= 2, "Maximum value of range needs to be 2 or smaller");
        return new SoundDefinition(sound, pitchRange);
    }
}
