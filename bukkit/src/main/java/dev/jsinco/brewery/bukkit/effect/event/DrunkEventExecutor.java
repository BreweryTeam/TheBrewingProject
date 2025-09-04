package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.api.event.*;
import dev.jsinco.brewery.api.event.step.*;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.effect.named.*;
import dev.jsinco.brewery.bukkit.effect.step.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class DrunkEventExecutor {

    private final Map<UUID, List<List<EventStep>>> onJoinServerExecutions = new HashMap<>();
    private final Map<UUID, List<List<EventStep>>> onDeathExecutions = new HashMap<>();
    private final Map<UUID, List<List<EventStep>>> onDamageExecutions = new HashMap<>();
    private final Map<UUID, Map<String, List<List<EventStep>>>> onJoinedWorldExecutions = new HashMap<>();

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
        registry.register(ConsumeStep.class, stepProperty -> new ConsumeStepExecutable(stepProperty.modifier(), stepProperty.incrementValue()));
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
        } else if (event instanceof CustomEvent.Keyed customEvent) {
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

    public void addConditionalWaitExecution(UUID playerUuid, List<EventStep> events, Condition condition) {
        switch (condition) {
            case Condition.Died died ->
                    onDeathExecutions.computeIfAbsent(playerUuid, ignored -> new ArrayList<>()).add(events);
            case Condition.HasPermission hasPermission -> {
                throw new IllegalStateException("Can not schedule 'has permission' conditions");
            }
            case Condition.JoinedServer joinedServer -> {
                if (Bukkit.getPlayer(playerUuid) != null) {
                    doDrunkEvents(playerUuid, events);
                    return;
                }
                onJoinServerExecutions.computeIfAbsent(playerUuid, ignored -> new ArrayList<>()).add(events);
            }
            case Condition.JoinedWorld joinedWorld -> {
                if (Bukkit.getPlayer(playerUuid) instanceof Player player && player.getWorld().getName().equals(joinedWorld.worldName())) {
                    doDrunkEvents(playerUuid, events);
                    return;
                }
                onJoinedWorldExecutions.computeIfAbsent(playerUuid, ignored -> new HashMap<>())
                        .computeIfAbsent(joinedWorld.worldName(), ignored -> new ArrayList<>()).add(events);
            }
            case Condition.TookDamage tookDamage ->
                    onDamageExecutions.computeIfAbsent(playerUuid, ignored -> new ArrayList<>()).add(events);
        }
    }

    public void clear(UUID playerUuid) {
        onJoinServerExecutions.remove(playerUuid);
    }

    public void clear() {
        onJoinServerExecutions.clear();
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
        eventStepListQueue.forEach(eventSteps -> doDrunkEvents(playerUuid, eventSteps));
    }
}
