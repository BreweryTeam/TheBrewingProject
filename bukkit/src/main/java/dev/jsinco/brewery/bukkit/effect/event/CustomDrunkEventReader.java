package dev.jsinco.brewery.bukkit.effect.event;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.event.*;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Registry;
import dev.jsinco.brewery.moment.Interval;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomDrunkEventReader {

    private CustomDrunkEventReader() {
        throw new IllegalStateException("Utility class");
    }

    public static List<CustomEvent> read(Map<String, Object> customEvents) {
        List<CustomEvent> output = new ArrayList<>();
        for (String key : customEvents.keySet()) {
            output.add(readEvent(customEvents, key, Set.of()));
        }
        return output;
    }

    private static CustomEvent readEvent(Map<String, Object> customEvents, String eventName, Set<String> banned) {
        if (banned.contains(eventName)) {
            throw new IllegalArgumentException("There's as an infinite loop in your events! The following events involved: " + banned);
        }
        Map<String, Object> eventData = (Map<String, Object>) customEvents.get(eventName);
        int alcohol = (int) eventData.getOrDefault("alcohol", 0);
        int toxin = (int) eventData.getOrDefault("toxin", 0);
        int probabilityWeight = (int) eventData.getOrDefault("probability-weight", 0);
        CustomEvent.Builder builder = new CustomEvent.Builder(BreweryKey.parse(eventName))
                .alcoholRequirement(alcohol)
                .toxinsRequirement(toxin)
                .probabilityWeight(probabilityWeight);
        List<Map<String, Object>> steps = (List<Map<String, Object>>) eventData.get("steps");
        for (Map<String, Object> step : steps) {
            String stepType = (String) step.get("type");
            NamedDrunkEvent namedDrunkEvent = Registry.DRUNK_EVENT.get(BreweryKey.parse(stepType));
            if (namedDrunkEvent != null) {
                builder.addStep(namedDrunkEvent);
            } else if (stepType.equals("event")) {
                builder.addStep(readEvent(customEvents, (String) step.get("event"), Stream.concat(banned.stream(), Stream.of((String) step.get("event"))).collect(Collectors.toSet())));
            } else if (stepType.equals("command")) {
                SendCommand.CommandSenderType senderType = SendCommand.CommandSenderType.valueOf(step.getOrDefault("as", "server").toString().toUpperCase(Locale.ROOT));
                String command = Preconditions.checkNotNull((String) step.get("command"), "Command can not be null in event step for event: " + eventName);
                builder.addStep(new SendCommand(command, senderType));
            } else if (stepType.equals("wait")) {
                if (step.containsKey("condition")) {
                    builder.addStep(ConditionalWaitStep.parse((String) step.get("condition")));
                }
                if (step.containsKey("duration")) {
                    builder.addStep(WaitStep.parse((String) step.get("duration")));
                }
                if (!step.containsKey("duration") && !step.containsKey("condition")) {
                    throw new IllegalArgumentException("Expected duration or condition to be specified");
                }
            } else if (stepType.equals("potion")) {
                String effect = (String) step.get("effect");
                Interval amplifier = Interval.parse(step.get("amplifier"));
                Interval duration = Interval.parse(step.get("duration"));
                builder.addStep(new ApplyPotionEffect(effect, amplifier, duration));
            } else if (stepType.equals("consume")) {
                builder.addStep(new ConsumeStep(
                                (Integer) step.getOrDefault("alcohol", 0),
                                (Integer) step.getOrDefault("toxins", 0)
                        )
                );
            }
        }
        return builder.build();
    }
}
