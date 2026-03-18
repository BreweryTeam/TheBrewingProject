package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.time.Duration;
import dev.jsinco.brewery.time.TimeUtil;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class TicksDurationSerializer implements ObjectSerializer<Duration.Ticks> {
    @Override
    public boolean supports(@NonNull Class<? super Duration.Ticks> type) {
        return Duration.Ticks.class == type;
    }

    @Override
    public void serialize(Duration.@NonNull Ticks object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(TimeUtil.minimalString(object.durationTicks(), TimeUtil.TimeUnit.SECONDS, Set.of(TimeUtil.TimeUnit.AGING_YEARS, TimeUtil.TimeUnit.COOKING_MINUTES)));
    }

    @Override
    public Duration.Ticks deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String durationString = data.getValue(String.class);
        if (durationString == null) {
            return new Duration.Ticks(0L);
        }
        return new Duration.Ticks(TimeUtil.parse(durationString, TimeUtil.TimeUnit.MINUTES));
    }
}
