package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.api.event.CustomEvent;
import dev.jsinco.brewery.api.event.CustomEventRegistry;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.EventStepRegistry;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
import dev.jsinco.brewery.api.event.step.ApplyPotionEffect;
import dev.jsinco.brewery.api.event.step.Condition;
import dev.jsinco.brewery.api.event.step.ConditionalStep;
import dev.jsinco.brewery.api.event.step.ConditionalWaitStep;
import dev.jsinco.brewery.api.event.step.ConsumeStep;
import dev.jsinco.brewery.api.event.step.CustomEventCompleted;
import dev.jsinco.brewery.api.event.step.CustomEventStep;
import dev.jsinco.brewery.api.event.step.SendCommand;
import dev.jsinco.brewery.api.event.step.Teleport;
import dev.jsinco.brewery.api.event.step.WaitStep;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.event.DrunkEventInitiateEvent;
import dev.jsinco.brewery.bukkit.effect.named.ChickenNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.named.DrunkMessageNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.named.DrunkenWalkNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.named.FeverNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.named.HallucinationNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.named.KaboomNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.named.NauseaNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.named.PassOutNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.named.PukeNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.named.StumbleNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.named.TeleportNamedExecutable;
import dev.jsinco.brewery.bukkit.effect.step.ApplyPotionEffectExecutable;
import dev.jsinco.brewery.bukkit.effect.step.ConditionalStepExecutable;
import dev.jsinco.brewery.bukkit.effect.step.ConditionalWaitStepExecutable;
import dev.jsinco.brewery.bukkit.effect.step.ConsumeStepExecutable;
import dev.jsinco.brewery.bukkit.effect.step.CustomEventExecutable;
import dev.jsinco.brewery.bukkit.effect.step.SendCommandExecutable;
import dev.jsinco.brewery.bukkit.effect.step.TeleportExecutable;
import dev.jsinco.brewery.bukkit.effect.step.WaitStepExecutable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DrunkEventExecutor {

    private final Map<UUID, List<List<EventStep>>> onJoinServerExecutions = new HashMap<>();
    private final Map<UUID, List<List<EventStep>>> onDeathExecutions = new HashMap<>();
    private final Map<UUID, List<List<EventStep>>> onDamageExecutions = new HashMap<>();
    private final Map<UUID, Map<String, List<List<EventStep>>>> onJoinedWorldExecutions = new HashMap<>();
    private final Map<UUID, Map<BreweryKey, Integer>> runningCustomEvents = new HashMap<>();

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
        registry.register(ConditionalStep.class, stepProperty -> new ConditionalStepExecutable(stepProperty.condition()));
        registry.register(ConsumeStep.class, stepProperty -> new ConsumeStepExecutable(stepProperty.modifiers()));
        registry.register(SendCommand.class, stepProperty -> new SendCommandExecutable(stepProperty.command(), stepProperty.senderType()));
        registry.register(Teleport.class, stepProperty -> new TeleportExecutable(stepProperty.location()));
        registry.register(WaitStep.class, stepProperty -> new WaitStepExecutable(stepProperty.durationTicks()));
        CustomEventRegistry eventRegistry = TheBrewingProject.getInstance().getCustomDrunkEventRegistry();
        registry.register(CustomEventStep.class, stepProperty -> {
                    List<EventStep> steps = new ArrayList<>(eventRegistry.getCustomEvent(stepProperty.customEventKey()).getSteps());
                    steps.add(new EventStep.Builder().addProperty(new CustomEventCompleted(stepProperty.customEventKey())).build());
                    return new CustomEventExecutable(steps, stepProperty.customEventKey());
                }
        );
    }

    public void doDrunkEvent(UUID playerUuid, DrunkEvent event) {
        DrunkEventInitiateEvent eventInitiateEvent = new DrunkEventInitiateEvent(event, Bukkit.getPlayer(playerUuid));
        if (!eventInitiateEvent.callEvent()) {
            return;
        }
        if (event instanceof CustomEvent.Keyed customEvent) {
            if (runningCustomEvents.getOrDefault(playerUuid, Map.of()).getOrDefault(customEvent.key(), 0) > 0) {
                return;
            }
            List<EventStep> eventSteps = new ArrayList<>(customEvent.getSteps());
            eventSteps.add(new EventStep.Builder().addProperty(new CustomEventCompleted(customEvent.key())).build());
            doDrunkEvents(playerUuid, eventSteps, customEvent.key(), true);
        } else if (event instanceof EventStepProperty eventStepProperty) {
            doDrunkEvents(playerUuid, List.of(
                    new EventStep.Builder().addProperty(eventStepProperty).build()
            ), null, false);
        }
    }

    public void doDrunkEvents(UUID playerUuid, List<? extends EventStep> events, @Nullable BreweryKey eventId, boolean incrementEventCounter) {
        EventStepRegistry registry = TheBrewingProject.getInstance().getEventStepRegistry();
        if (incrementEventCounter && eventId != null) {
            runningCustomEvents
                    .computeIfAbsent(playerUuid, ignored -> new HashMap<>())
                    .compute(eventId, (ignored, integer) -> integer == null ? 1 : integer + 1);
        }
        boolean stopping = false;
        for (int i = 0; i < events.size(); i++) {
            final EventStep event = events.get(i);
            event.properties().stream()
                    .filter(CustomEventCompleted.class::isInstance)
                    .map(CustomEventCompleted.class::cast)
                    .forEach(customEventCompleted -> runningCustomEvents
                            .computeIfAbsent(playerUuid, ignored -> new HashMap<>())
                            .compute(customEventCompleted.eventKey(), (ignored, integer) -> integer == null || integer == 0 ? 0 : integer - 1)
                    );
            if (stopping) {
                continue;
            }
            List<EventPropertyExecutable> properties = event.properties().stream()
                    .filter(eventStepProperty -> !(eventStepProperty instanceof CustomEventCompleted))
                    .map(registry::toExecutable)
                    .sorted(Comparator.comparing(EventPropertyExecutable::priority, Integer::compareTo))
                    .toList();
            for (EventPropertyExecutable eventPropertyExecutable : properties) {
                EventPropertyExecutable.ExecutionResult result = eventPropertyExecutable.execute(playerUuid, events, i);
                if (result == EventPropertyExecutable.ExecutionResult.STOP_EXECUTION) {
                    stopping = true;
                    break;
                } else if (result == EventPropertyExecutable.ExecutionResult.WAIT_UNTIL_CONDITION) {
                    return;
                }
            }
        }
    }

    public void addConditionalWaitExecution(UUID playerUuid, List<EventStep> events, Condition condition) {
        switch (condition) {
            case Condition.Died died ->
                    onDeathExecutions.computeIfAbsent(playerUuid, ignored -> new ArrayList<>()).add(events);
            case Condition.JoinedServer joinedServer -> {
                if (Bukkit.getPlayer(playerUuid) != null) {
                    doDrunkEvents(playerUuid, events, null, false);
                    return;
                }
                onJoinServerExecutions.computeIfAbsent(playerUuid, ignored -> new ArrayList<>()).add(events);
            }
            case Condition.JoinedWorld joinedWorld -> {
                if (Bukkit.getPlayer(playerUuid) instanceof Player player && player.getWorld().getName().equals(joinedWorld.worldName())) {
                    doDrunkEvents(playerUuid, events, null, false);
                    return;
                }
                onJoinedWorldExecutions.computeIfAbsent(playerUuid, ignored -> new HashMap<>())
                        .computeIfAbsent(joinedWorld.worldName(), ignored -> new ArrayList<>()).add(events);
            }
            case Condition.TookDamage tookDamage ->
                    onDamageExecutions.computeIfAbsent(playerUuid, ignored -> new ArrayList<>()).add(events);
            default -> throw new IllegalStateException("Can not schedule condition: " + condition);
        }
    }

    public void clear(UUID playerUuid) {
        onJoinServerExecutions.remove(playerUuid);
    }

    public void clear() {
        onJoinServerExecutions.clear();
        onDeathExecutions.clear();
        onDamageExecutions.clear();
        onJoinedWorldExecutions.clear();
    }

    public void onPlayerJoinServer(UUID playerUuid) {
        executeQueue(playerUuid, onJoinServerExecutions.remove(playerUuid));
    }

    public void onPlayerJoinWorld(UUID playerUuid, World world) {
        executeQueue(playerUuid, onJoinedWorldExecutions.computeIfAbsent(playerUuid, ignored -> new HashMap<>()).remove(world.getName()));
    }

    public void onDamage(UUID playerUuid) {
        executeQueue(playerUuid, onDamageExecutions.remove(playerUuid));
    }

    public void onDeathExecutions(UUID playerUuid) {
        executeQueue(playerUuid, onDeathExecutions.remove(playerUuid));
    }

    private void executeQueue(UUID playerUuid, List<List<EventStep>> eventStepListQueue) {
        if (eventStepListQueue == null) {
            return;
        }
        eventStepListQueue.forEach(eventSteps -> doDrunkEvents(playerUuid, eventSteps, null, false));
    }
}
