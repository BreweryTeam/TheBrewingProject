package dev.jsinco.brewery.bukkit.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.event.*;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;

import javax.annotation.Nullable;
import java.util.*;

public class EventUtil {

    public static Optional<DrunkEvent> fromData(EventData data) {
        TheBrewingProject theBrewingProject = TheBrewingProject.getInstance();
        BreweryKey key = data.key();
        ModifierExpression modifierExpressionOverride = null;
        try {
            String temp = data.data(EventData.PROBABILITY_KEY, MetaDataType.STRING);
            if (temp != null) {
                modifierExpressionOverride = new ModifierExpression(temp);
            }
        } catch (Exception ignored) {
        }
        final ModifierExpression finalProbabilityWeight = modifierExpressionOverride;
        return Optional.<DrunkEvent>ofNullable(BreweryRegistry.DRUNK_EVENT.get(key))
                .or(() -> Optional.ofNullable(theBrewingProject.getCustomDrunkEventRegistry().getCustomEvent(key)))
                .or(() -> theBrewingProject.getIntegrationManager().getIntegrationRegistry()
                        .getIntegrations(IntegrationTypes.EVENT)
                        .stream()
                        .map(eventIntegration -> eventIntegration.convertToEvent(data))
                        .flatMap(Optional::stream)
                        .findFirst()
                ).map(drunkEvent -> applyProbability(drunkEvent, finalProbabilityWeight));
    }

    private static DrunkEvent applyProbability(DrunkEvent drunkEvent, @Nullable ModifierExpression probability) {
        return probability == null || !(drunkEvent instanceof EventStepProperty property) ? drunkEvent : new CustomEvent.Builder()
                .probability(new EventProbability(probability, Map.of()))
                .addStep(new EventStep.Builder().addProperty(property).build())
                .build(drunkEvent.key());
    }

    public static List<BreweryKey> listAll() {
        ImmutableList.Builder<BreweryKey> keyBuilder = ImmutableList.builder();
        Streams.concat(TheBrewingProject.getInstance().getCustomDrunkEventRegistry().events().stream(), BreweryRegistry.DRUNK_EVENT.values().stream())
                .filter(Objects::nonNull)
                .map(DrunkEvent::key)
                .forEach(keyBuilder::add);
        TheBrewingProject.getInstance().getIntegrationManager().getIntegrationRegistry()
                .getIntegrations(IntegrationTypes.EVENT)
                .stream()
                .map(EventIntegration::listEventKeys)
                .flatMap(Collection::stream)
                .forEach(keyBuilder::add);
        return keyBuilder.build();
    }
}
