package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.effect.named.*;
import dev.jsinco.brewery.bukkit.effect.step.*;
import dev.jsinco.brewery.event.*;
import dev.jsinco.brewery.event.step.*;

import java.util.*;

public class DrunkEventExecutor {

    private final Map<UUID, List<EventStep>> onJoinExecutions = new HashMap<>();

    public DrunkEventExecutor() {
        EventStepRegistry registry = TheBrewingProject.getInstance().getEventStepRegistry();
        registry.register(NamedDrunkEvent.fromKey("chicken"), ChickenNamedExecutable::new);
        registry.register(NamedDrunkEvent.fromKey("drunken_walk"), DrunkenWalkNamedExecutable::new);
        registry.register(NamedDrunkEvent.fromKey("drunk_message"), DrunkMessageNamedExecutable::new);
        registry.register(NamedDrunkEvent.fromKey("fever"), FeverNamedExecutable::new);
        registry.register(NamedDrunkEvent.fromKey("hallucination"), HallucinationNamedExecutable::new);
        registry.register(NamedDrunkEvent.fromKey("kaboom"), KaboomNamedExecutable::new);
        registry.register(NamedDrunkEvent.fromKey("nausea"), NauseaNamedExecutable::new);
        registry.register(NamedDrunkEvent.fromKey("pass_out"), PassOutNamedExecutable::new);
        registry.register(NamedDrunkEvent.fromKey("puke"), PukeNamedExecutable::new);
        registry.register(NamedDrunkEvent.fromKey("stumble"), StumbleNamedExecutable::new);
        registry.register(NamedDrunkEvent.fromKey("teleport"), TeleportNamedExecutable::new);
        registry.register(ApplyPotionEffect.class, stepProperty -> new ApplyPotionEffectExecutable(stepProperty.potionEffectName(), stepProperty.amplifierBounds(), stepProperty.durationBounds()));
        registry.register(ConditionalWaitStep.class, stepProperty -> new ConditionalWaitStepExecutable(stepProperty.getCondition()));
        registry.register(ConsumeStep.class, stepProperty -> new ConsumeStepExecutable(stepProperty.alcohol(), stepProperty.toxins()));
        registry.register(SendCommand.class, stepProperty -> new SendCommandExecutable(stepProperty.command(), stepProperty.senderType()));
        registry.register(Teleport.class, stepProperty -> new TeleportExecutable(stepProperty.location()));
        registry.register(WaitStep.class, stepProperty -> new WaitStepExecutable(stepProperty.durationTicks()));
        CustomEventRegistry eventRegistry = TheBrewingProject.getInstance().getCustomDrunkEventRegistry();
        registry.register(CustomEventStep.class, stepProperty -> new CustomEventExecutable(
                eventRegistry.getCustomEvent(stepProperty.customEventKey()).getSteps()
        ));
    }

    public void doDrunkEvent(UUID playerUuid, DrunkEvent event) {
        if (event instanceof NamedDrunkEvent namedDrunkEvent) {
            doDrunkEvents(playerUuid, List.of(
                    new EventStep.Builder().addProperty(namedDrunkEvent).build()
            ));
        } else if (event instanceof CustomEvent customEvent) {
            doDrunkEvents(playerUuid, customEvent.getSteps());
        }
    }

    public void doDrunkEvents(UUID playerUuid, List<? extends EventStep> events) {
        EventStepRegistry registry = TheBrewingProject.getInstance().getEventStepRegistry();

        for (int i = 0; i < events.size(); i++) {
            final EventStep event = events.get(i);
            List<EventPropertyExecutable> properties = event.properties().stream()
                    .map(registry::toExecutable)
                    .sorted(Comparator.comparing(EventPropertyExecutable::priority, Integer::compareTo))
                    .toList();
            for (EventPropertyExecutable eventPropertyExecutable : properties) {
                if (eventPropertyExecutable.execute(playerUuid, events, i) == EventPropertyExecutable.ExecutionResult.STOP_EXECUTION) {
                    return;
                }
            }
        }
    }

    public void add(UUID playerUuid, List<EventStep> events) {
        onJoinExecutions.put(playerUuid, events);
    }

    public void clear(UUID playerUuid) {
        onJoinExecutions.remove(playerUuid);
    }

    public void clear() {
        onJoinExecutions.clear();
    }

    public void onPlayerJoin(UUID playerUuid) {
        List<EventStep> eventStepList = onJoinExecutions.get(playerUuid);
        if (eventStepList == null) {
            return;
        }
        doDrunkEvents(playerUuid, eventStepList);
    }
}
