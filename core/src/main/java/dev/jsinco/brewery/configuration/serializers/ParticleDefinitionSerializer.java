package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.math.RangeD;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.configuration.ParticleDefinition;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NonNull;

public class ParticleDefinitionSerializer implements ObjectSerializer<ParticleDefinition> {

    private static final RangeD FULL_RANGE = new RangeD(0D, 1D);

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type == ParticleDefinition.class;
    }

    @Override
    public void serialize(@NonNull ParticleDefinition object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        StringBuilder outputBuilder = new StringBuilder();
        if (object.quality() != null) {
            outputBuilder.append(switch (object.quality()) {
                case BAD -> "+";
                case GOOD -> "++";
                case EXCELLENT -> "+++";
            });
        }
        outputBuilder.append(object.particleKey().minimalized(Key.MINECRAFT_NAMESPACE));
        outputBuilder.append('/');
        outputBuilder.append(object.probability());
        if (object.range() == null || object.range().equals(FULL_RANGE)) {
            data.setValue(outputBuilder.toString());
            return;
        }
        outputBuilder.append('/');
        outputBuilder.append(object.range());
        data.setValue(outputBuilder.toString());
    }

    @Override
    public ParticleDefinition deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String input = data.getValue(String.class);
        Preconditions.checkArgument(input != null, "Expected a string for particle");
        String[] split = input.split("/");
        Preconditions.checkArgument(split.length <= 3 && split.length > 1, "Expected a string of the format <particle-name>/<chance>/<optional-effect-time>");
        String split0 = split[0];
        BreweryKey particleKey;
        BrewQuality brewQuality;
        if (!split0.startsWith("+")) {
            particleKey = BreweryKey.minecraft(split0.trim());
            brewQuality = null;
        } else {
            if (split0.startsWith("+++")) {
                brewQuality = BrewQuality.EXCELLENT;
            } else if (split0.startsWith("++")) {
                brewQuality = BrewQuality.GOOD;
            } else {
                brewQuality = BrewQuality.BAD;
            }
            particleKey = BreweryKey.minecraft(split0.replaceAll("^\\++", "").trim());
        }
        double chance = Double.parseDouble(split[1]);
        Preconditions.checkArgument(chance > 0 && chance <= 1D, "Chance has to be within the range (0, 1]");
        if (split.length == 2) {
            return new ParticleDefinition(particleKey, chance, null, brewQuality);
        }
        RangeD rangeD = RangeD.fromString(split[2]);
        Preconditions.checkArgument(rangeD.min() != null && rangeD.min() >= 0D, "Expected min range to be larger than or equal to 0");
        return new ParticleDefinition(particleKey, chance, rangeD, brewQuality);
    }
}
