package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.event.CustomEventRegistry;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.step.CustomEvent;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Logger;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class EventRegistrySerializer implements ObjectSerializer<CustomEventRegistry> {

    private <T> T assertObject(@Nullable Object object, Class<T> type, Predicate<T> test, String errorMessage) {
        Preconditions.checkArgument(type.isInstance(object), "Expected an " + type.getSimpleName());
        T t = type.cast(object);
        Preconditions.checkArgument(test.test(t), errorMessage);
        return t;
    }

    private <T> T assertObject(@Nullable Object object, Class<T> type) {
        Preconditions.checkArgument(type.isInstance(object), "Expected an " + type.getSimpleName());
        return type.cast(object);
    }

    private CustomEvent readEvent(DeserializationData customEvents, String eventName, Set<String> banned) {
        try {
            Preconditions.checkArgument(eventName != null, "Undefined event name");
            if (banned.contains(eventName)) {
                throw new IllegalArgumentException("There's as an infinite loop in your events! The following events are involved: " + banned);
            }
            Map<String, Object> eventData = customEvents.getAsMap(eventName, String.class, Object.class);
            int alcohol = assertObject(eventData.getOrDefault("alcohol", 0), Integer.class);
            int toxin = assertObject(eventData.getOrDefault("toxins", 0), Integer.class);
            int probabilityWeight = assertObject(eventData.getOrDefault("probability-weight", 0), Integer.class);
            CustomEvent.Builder builder = new CustomEvent.Builder(BreweryKey.parse(eventName))
                    .alcoholRequirement(alcohol)
                    .toxinsRequirement(toxin)
                    .probabilityWeight(probabilityWeight);

            List<EventStep> steps = customEvents.getAsList("steps", EventStep.class);
            steps.forEach(builder::addStep);
            return builder.build();
        } catch (IllegalArgumentException e) {
            Logger.logErr("Exception when reading custom event: " + eventName);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean supports(@NonNull Class<? super CustomEventRegistry> type) {
        return CustomEventRegistry.class == type;
    }

    @Override
    public void serialize(@NonNull CustomEventRegistry object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        Map<String, Map<String, Object>> output = new HashMap<>();
        for (CustomEvent event : object.events()) {
            Map<String, Object> eventData = new HashMap<>();
            if (event.alcoholRequirement() != 0) {
                eventData.put("alcohol", event.alcoholRequirement());
            }
            if (event.toxinsRequirement() != 0) {
                eventData.put("toxins", event.toxinsRequirement());
            }
            if (event.probabilityWeight() != 0) {
                eventData.put("probability-weight", event.probabilityWeight());
            }
            eventData.put("steps", event.getSteps());
            output.put(event.key().key(), eventData);
        }
        data.setValue(output);
    }

    @Override
    public CustomEventRegistry deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        CustomEventRegistry output = new CustomEventRegistry();
        Collection<String> children = data.asMap().keySet();
        for (String child : children) {
            output.registerCustomEvent(readEvent(data, child, Set.of()));
        }
        return output;
    }

    private interface FunctionThatThrows<T, U, E extends Throwable> {

        U apply(T t) throws E;
    }
}
