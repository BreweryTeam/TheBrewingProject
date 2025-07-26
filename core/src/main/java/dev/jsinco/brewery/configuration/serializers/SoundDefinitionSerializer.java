package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.math.RangeF;
import dev.jsinco.brewery.sound.SoundDefinition;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Objects;

public class SoundDefinitionSerializer implements ObjectSerializer<SoundDefinition> {


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

    @Override
    public boolean supports(@NonNull Class<? super SoundDefinition> type) {
        return SoundDefinition.class == type;
    }

    @Override
    public void serialize(@NonNull SoundDefinition object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        List<SoundDefinition.SoundSetting> sounds = object.sounds();
        if (sounds.size() == 1) {
            data.setValue(serializeSoundSetting(sounds.getFirst()));
            return;
        }
        data.setValue(sounds.stream()
                .map(this::serializeSoundSetting)
                .toList()
        );
    }

    @Override
    public SoundDefinition deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        if (List.class.isAssignableFrom(generics.getType())) {
            List<String> serialized = data.getValueAsList(String.class);
            if (serialized == null || serialized.isEmpty()) {
                return null;
            }
            return new SoundDefinition(
                    serialized.stream()
                            .filter(string -> !string.isBlank())
                            .map(SoundDefinitionSerializer::parseSoundSetting).toList()
            );
        }
        String serialized = data.getValue(String.class);
        if (serialized == null || serialized.isBlank()) {
            return null;
        }
        return new SoundDefinition(List.of(parseSoundSetting(serialized)));
    }
}
