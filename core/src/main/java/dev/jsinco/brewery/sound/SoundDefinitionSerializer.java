package dev.jsinco.brewery.sound;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.math.RangeF;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class SoundDefinitionSerializer implements TypeSerializer<SoundDefinition> {


    @Override
    public SoundDefinition deserialize(@NotNull Type type, ConfigurationNode node) throws SerializationException {
        if (node.isList()) {
            List<String> serialized = node.getList(String.class);
            if (serialized == null || serialized.isEmpty()) {
                return null;
            }
            return new SoundDefinition(
                    serialized.stream().map(SoundDefinitionSerializer::parseSoundSetting).toList()
            );
        }
        String serialized = node.get(String.class);
        if (serialized == null || serialized.isBlank()) {
            return null;
        }
        try {
            return new SoundDefinition(List.of(parseSoundSetting(serialized)));
        } catch (IllegalArgumentException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable SoundDefinition obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null || obj.sounds().isEmpty()) {
            node.set(null);
            return;
        }
        if (obj.sounds().size() == 1) {
            node.set(serializeSoundSetting(obj.sounds().get(0)));
        }
        node.set(obj.sounds().stream()
                .map(this::serializeSoundSetting)
                .toList()
        );
    }

    private String serializeSoundSetting(SoundDefinition.SoundSetting soundSetting) {
        if (Objects.equals(soundSetting.pitch(), new RangeF(1F, 1F))) {
            return soundSetting.soundKey().toString();
        }
        if (soundSetting.pitch().min() == soundSetting.pitch().max()) {
            return soundSetting.soundKey().toString() + "/" + soundSetting.pitch();
        }
        return soundSetting.soundKey().toString() + "/" + soundSetting.pitch().min() + ";" + soundSetting.pitch().max();
    }

    public static SoundDefinition.SoundSetting parseSoundSetting(String string) {
        String[] split = string.split("/");

        if (split.length > 2) {
            throw new IllegalArgumentException("Invalid sound definition");
        }

        Key sound = Key.key(split[0]);
        RangeF pitchRange = split.length > 1 ? RangeF.fromString(split[1]) : new RangeF(1.0f, 1.0f);
        Preconditions.checkArgument(pitchRange.min() >= 0, "Minimum value of range needs to be 0 or larger");
        Preconditions.checkArgument(pitchRange.max() <= 2, "Maximum value of range needs to be 2 or smaller");
        return new SoundDefinition.SoundSetting(sound, pitchRange);
    }
}
