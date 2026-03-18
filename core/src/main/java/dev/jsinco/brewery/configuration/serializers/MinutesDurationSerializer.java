package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.time.Duration;
import dev.jsinco.brewery.time.TimeUtil;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class MinutesDurationSerializer implements ObjectSerializer<Duration.Minutes> {
    @Override
    public boolean supports(@NonNull Class<? super Duration.Minutes> type) {
        return Duration.Minutes.class == type;
    }

    @Override
    public void serialize(Duration.@NonNull Minutes object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(TimeUtil.minimalString(object.durationTicks(), TimeUtil.TimeUnit.MINUTES, Set.of(TimeUtil.TimeUnit.AGING_YEARS, TimeUtil.TimeUnit.COOKING_MINUTES)));
    }

    @Override
    public Duration.Minutes deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String durationString = data.getValue(String.class);
        if (durationString == null) {
            return new Duration.Minutes(0L);
        }
        return new Duration.Minutes(TimeUtil.parse(durationString, TimeUtil.TimeUnit.MINUTES));
    }
}
