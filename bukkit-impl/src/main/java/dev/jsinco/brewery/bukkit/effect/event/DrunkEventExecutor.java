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
import dev.jsinco.brewery.bukkit.effect.step.CustomEventCompletedExecutable;
import dev.jsinco.brewery.bukkit.effect.step.CustomEventExecutable;
import dev.jsinco.brewery.bukkit.effect.step.SendCommandExecutable;
import dev.jsinco.brewery.bukkit.effect.step.TeleportExecutable;
import dev.jsinco.brewery.bukkit.effect.step.WaitStepExecutable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DrunkEventExecutor {

    private final Map<UUID, List<List<EventPropertyExecutable>>> onJoinServerExecutions = new ConcurrentHashMap<>();
    private final Map<UUID, List<List<EventPropertyExecutable>>> onDeathExecutions = new ConcurrentHashMap<>();
    private final Map<UUID, List<List<EventPropertyExecutable>>> onDamageExecutions = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, List<List<EventPropertyExecutable>>>> onJoinedWorldExecutions = new ConcurrentHashMap<>();
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
        registry.register(CustomEventCompleted.class, CustomEventCompletedExecutable::new);
        CustomEventRegistry eventRegistry = TheBrewingProject.getInstance().getCustomDrunkEventRegistry();
        registry.register(CustomEventStep.class, stepProperty -> {
                    List<EventStep> steps = eventRegistry.getCustomEvent(stepProperty.customEventKey()).getSteps();
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
            synchronized (runningCustomEvents) {
                if (runningCustomEvents.getOrDefault(playerUuid, Map.of()).getOrDefault(customEvent.key(), 0) > 0) {
                    return;
                }
            }
            List<EventStep> eventSteps = new ArrayList<>(customEvent.getSteps());
            doDrunkEvents(playerUuid, eventSteps, customEvent.key(), true);
        } else if (event instanceof EventStepProperty eventStepProperty) {
            doDrunkEvents(playerUuid, List.of(
                    new EventStep.Builder().addProperty(eventStepProperty).build()
            ), null, false);
        }
    }

    public static List<EventPropertyExecutable> unwrap(List<? extends EventStep> eventSteps) {
        EventStepRegistry registry = TheBrewingProject.getInstance().getEventStepRegistry();
        return eventSteps
                .stream()
                .map(EventStep::properties)
                .flatMap(eventStepProperties -> eventStepProperties
                        .stream()
                        .map(registry::toExecutable)
                        .sorted(Comparator.comparing(EventPropertyExecutable::priority, Integer::compareTo))
                ).toList();
    }

    public void doDrunkEvents(UUID playerUuid, List<? extends EventStep> steps, @Nullable BreweryKey eventId, boolean incrementEventCounter) {
        List<EventPropertyExecutable> stepsToRun = new ArrayList<>(unwrap(steps));
        if (stepsToRun.isEmpty()) {
            return;
        }
        if (incrementEventCounter && eventId != null) {
            synchronized (runningCustomEvents) {
                runningCustomEvents
                        .computeIfAbsent(playerUuid, ignored -> new HashMap<>())
                        .compute(eventId, (ignored, integer) -> integer == null ? 1 : integer + 1);
            }
        }
        if (eventId != null) {
            stepsToRun.add(new CustomEventCompletedExecutable(new CustomEventCompleted(eventId)));
        }
        EventPropertyExecutable last = stepsToRun.getLast();
        List<EventPropertyExecutable> output = stepsToRun.stream()
                .map(executable -> executable == last ? executable : executable.withSkipPoint(last))
                .toList();
        doDrunkEvents(playerUuid, output);
    }

    public void doDrunkEvents(UUID playerUuid, List<EventPropertyExecutable> executables) {
        if (executables.isEmpty()) {
            return;
        }
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                TheBrewingProject.getInstance(),
                new EventTask(new LinkedList<>(executables), playerUuid, this),
                1L,
                1L
        );
    }

    public void unregisterEvents(UUID playerUuid, List<EventPropertyExecutable> steps) {
        if (steps.isEmpty() || !(steps.getLast() instanceof CustomEventCompletedExecutable(
                CustomEventCompleted eventCompleted
        ))) {
            return;
        }

        unregisterEvent(playerUuid, eventCompleted.eventKey());
    }

    public void unregisterEvent(UUID playerUuid, BreweryKey key) {
        synchronized (runningCustomEvents) {
            runningCustomEvents
                    .computeIfAbsent(playerUuid, ignored2 -> new HashMap<>())
                    .compute(key, (ignored2, integer) -> integer == null || integer == 0 ? 0 : integer - 1);
        }
    }

    public void addConditionalWaitExecution(UUID playerUuid, List<EventPropertyExecutable> events, Condition condition) {
        switch (condition) {
            case Condition.Died died -> {
                onDeathExecutions.computeIfAbsent(playerUuid, ignored -> Collections.synchronizedList(new ArrayList<>())).add(events);
            }
            case Condition.JoinedServer joinedServer -> {
                if (Bukkit.getPlayer(playerUuid) != null) {
                    doDrunkEvents(playerUuid, events);
                    return;
                }
                onJoinServerExecutions.computeIfAbsent(playerUuid, ignored -> Collections.synchronizedList(new ArrayList<>())).add(events);

            }
            case Condition.JoinedWorld joinedWorld -> {
                if (Bukkit.getPlayer(playerUuid) instanceof Player player && player.getWorld().getName().equals(joinedWorld.worldName())) {
                    doDrunkEvents(playerUuid, events);
                    return;
                }
                onJoinedWorldExecutions.computeIfAbsent(playerUuid, ignored -> new ConcurrentHashMap<>())
                        .computeIfAbsent(joinedWorld.worldName(), ignored -> Collections.synchronizedList(new ArrayList<>())).add(events);

            }
            case Condition.TookDamage tookDamage -> {
                onDamageExecutions.computeIfAbsent(playerUuid, ignored -> Collections.synchronizedList(new ArrayList<>())).add(events);
            }
            default -> throw new IllegalStateException("Can not schedule condition: " + condition);
        }
    }

    public void clear(UUID playerUuid) {
        onJoinServerExecutions.remove(playerUuid);
        onDeathExecutions.remove(playerUuid);
        onDamageExecutions.remove(playerUuid);
        onJoinedWorldExecutions.remove(playerUuid);
        runningCustomEvents.remove(playerUuid);
    }

    public void clear() {
        onJoinServerExecutions.clear();
        onDeathExecutions.clear();
        onDamageExecutions.clear();
        onJoinedWorldExecutions.clear();
        runningCustomEvents.clear();
    }

    public void onPlayerJoinServer(UUID playerUuid) {
        executeQueue(playerUuid, onJoinServerExecutions.remove(playerUuid));
    }

    public void onPlayerJoinWorld(UUID playerUuid, World world) {
        executeQueue(playerUuid, onJoinedWorldExecutions.computeIfAbsent(playerUuid, ignored -> new ConcurrentHashMap<>()).remove(world.getName()));
    }

    public void onDamage(UUID playerUuid) {
        executeQueue(playerUuid, onDamageExecutions.remove(playerUuid));
    }

    public void onDeathExecutions(UUID playerUuid) {
        executeQueue(playerUuid, onDeathExecutions.remove(playerUuid));
    }

    private void executeQueue(UUID playerUuid, List<List<EventPropertyExecutable>> eventStepListQueue) {
        if (eventStepListQueue == null) {
            return;
        }
        eventStepListQueue.forEach(eventSteps -> doDrunkEvents(playerUuid, eventSteps));
    }

}
