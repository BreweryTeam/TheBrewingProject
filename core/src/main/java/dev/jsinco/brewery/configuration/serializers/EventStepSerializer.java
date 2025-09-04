package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
import dev.jsinco.brewery.api.event.step.*;
import dev.jsinco.brewery.api.moment.Interval;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventStepSerializer implements ObjectSerializer<EventStep> {
    @Override
    public boolean supports(@NonNull Class<? super EventStep> type) {
        return EventStep.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull EventStep object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        Map<String, Object> output = new HashMap<>();
        for (EventStepProperty property : object.properties()) {
            output.putAll(switch (property) {
                case ApplyPotionEffect applyPotionEffect -> Map.of(
                        "effect", applyPotionEffect.potionEffectName(),
                        "duration", applyPotionEffect.durationBounds().asString(),
                        "amplifier", applyPotionEffect.amplifierBounds().asString()
                );
                case ConditionalWaitStep conditionalWaitStep -> Map.of(
                        "wait-condition", conditionalWaitStep.condition()
                );
                case ConditionalStep conditionalStep -> Map.of(
                        "if-condition", conditionalStep.condition()
                );
                case ConsumeStep consumeStep -> Map.of(
                        consumeStep.modifier().name(), consumeStep.incrementValue()
                );
                case CustomEventStep customEvent -> Map.of(
                        "event", customEvent.customEventKey().key()
                );
                case NamedDrunkEvent namedDrunkEvent -> Map.of(
                        "named-event", namedDrunkEvent.key().key()
                );
                case SendCommand sendCommand -> Map.of(
                        "as", sendCommand.senderType().toString().toLowerCase(Locale.ROOT),
                        "command", sendCommand.command()
                );
                case Teleport teleport -> Map.of(
                        "location", teleport.location()
                );
                case WaitStep waitStep -> Map.of("wait-duration", waitStep.durationTicks() + "t");
                default -> throw new IllegalArgumentException("Unsupported event step: " + property);
            });
        }
        data.setValue(output);
    }

    @Override
    public EventStep deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        EventStep.Builder eventStepBuilder = new EventStep.Builder();
        // Backwards compatibility
        if (data.containsKey("type")) {
            NamedDrunkEvent namedDrunkEvent = BreweryRegistry.DRUNK_EVENT.get(BreweryKey.parse(data.get("type", String.class)));
            if (namedDrunkEvent != null) {
                eventStepBuilder.addProperty(namedDrunkEvent);
            }
        }
        if (data.containsKey("named-event")) {
            String string = data.get("named-event", String.class);
            NamedDrunkEvent namedDrunkEvent = BreweryRegistry.DRUNK_EVENT.get(BreweryKey.parse(string));
            Preconditions.checkArgument(namedDrunkEvent != null, "Unknown predefined drunk event: " + string);
            eventStepBuilder.addProperty(namedDrunkEvent);
        }
        if (data.containsKey("event")) {
            NamedDrunkEvent namedDrunkEvent = BreweryRegistry.DRUNK_EVENT.get(BreweryKey.parse(data.get("event", String.class)));
            eventStepBuilder.addProperty(namedDrunkEvent != null ? namedDrunkEvent : new CustomEventStep(BreweryKey.parse(data.get("event", String.class))));
        }
        if (data.containsKey("command")) {
            SendCommand.CommandSenderType senderType = data.get("as", SendCommand.CommandSenderType.class);
            String command = data.get("command", String.class);
            Preconditions.checkArgument(command != null, "Command can not be empty");
            eventStepBuilder.addProperty(new SendCommand(command, senderType == null ? SendCommand.CommandSenderType.SERVER : senderType));
        }
        Preconditions.checkArgument(!data.containsKey("condition") && !data.containsKey("wait-condition"), "Duplicate condition types 'condition' and 'wait-condition'");
        if (data.containsKey("condition")) {
            Condition condition = data.get("condition", Condition.class);
            Preconditions.checkArgument(condition != null, "Condition can not be empty");
            Preconditions.checkArgument(condition.getClass() != Condition.HasPermission.class, "Unimplemented wait condition: has-permisison");
            eventStepBuilder.addProperty(new ConditionalWaitStep(condition));
        }
        if (data.containsKey("wait-condition")) {
            Condition condition = data.get("condition", Condition.class);
            Preconditions.checkArgument(condition != null, "Condition can not be empty");
            Preconditions.checkArgument(condition.getClass() != Condition.HasPermission.class, "Unimplemented wait condition: has-permisison");
            eventStepBuilder.addProperty(new ConditionalWaitStep(condition));
        }
        if (data.containsKey("if-condition")) {
            Condition condition = data.get("condition", Condition.class);
            Preconditions.checkArgument(condition != null, "Condition can not be empty");
            eventStepBuilder.addProperty(new ConditionalWaitStep(condition));
        }
        if (data.containsKey("duration") && !data.containsKey("effect")) {
            String duration = data.get("duration", String.class);
            Preconditions.checkArgument(duration != null, "Duration can not be empty");
            eventStepBuilder.addProperty(WaitStep.parse(duration));
        }
        if (data.containsKey("wait-duration")) {
            String duration = data.get("wait-duration", String.class);
            Preconditions.checkArgument(duration != null, "Duration can not be empty");
            eventStepBuilder.addProperty(WaitStep.parse(duration));
        }
        if (data.containsKey("effect")) {
            String effect = data.get("effect", String.class);
            Interval amplifier = data.get("amplifier", Interval.class);
            Interval duration = data.get("duration", Interval.class);
            eventStepBuilder.addProperty(new ApplyPotionEffect(effect,
                    amplifier == null ? new Interval(1, 1) : amplifier,
                    duration == null ? new Interval(10 * Moment.SECOND, 10 * Moment.SECOND) : duration
            ));
        }
        if (data.containsKey("location")) {
            BreweryLocation.Uncompiled breweryLocation = data.get("location", BreweryLocation.Uncompiled.class);
            Preconditions.checkArgument(breweryLocation != null, "Location can not be empty");
            eventStepBuilder.addProperty(new Teleport(breweryLocation));
        }
        DrunkenModifierSection.modifiers().drunkenModifiers()
                .stream()
                .filter(modifier -> data.containsKey(modifier.name()))
                .map(modifier -> new ConsumeStep(modifier, data.get(modifier.name(), Integer.class)))
                .forEach(eventStepBuilder::addProperty);
        EventStep eventStep = eventStepBuilder.build();
        if (eventStep.properties().isEmpty()) {
            throw new IllegalArgumentException("Unknown step type");
        }
        return eventStep;
    }
}
