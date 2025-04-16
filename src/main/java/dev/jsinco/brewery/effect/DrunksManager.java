package dev.jsinco.brewery.effect;

import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.PersistenceHandler;
import dev.jsinco.brewery.effect.event.CustomEventRegistry;
import dev.jsinco.brewery.effect.event.DrunkEvent;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.util.Registry;
import dev.jsinco.brewery.util.moment.Moment;
import dev.jsinco.brewery.util.random.RandomUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

public class DrunksManager<C> {

    private final CustomEventRegistry eventRegistry;
    private final PersistenceHandler<C> persistenceHandler;
    private final DrunkStateDataType<C> drunkStateDataType;
    private Set<BreweryKey> allowedEvents;
    private Map<UUID, DrunkState> drunks = new HashMap<>();
    @Getter
    private LongSupplier timeSupplier;
    private Map<Long, Map<UUID, DrunkEvent>> events = new HashMap<>();
    private Map<UUID, Long> plannedEvents = new HashMap<>();

    private static final Random RANDOM = new Random();

    public DrunksManager(CustomEventRegistry registry, Set<BreweryKey> allowedEvents, LongSupplier timeSupplier,
                         PersistenceHandler<C> persistenceHandler, DrunkStateDataType<C> drunkStateDataType) {
        this.eventRegistry = registry;
        this.allowedEvents = allowedEvents;
        this.timeSupplier = timeSupplier;
        this.persistenceHandler = persistenceHandler;
        this.drunkStateDataType = drunkStateDataType;
        loadDrunkStates();
    }

    private void loadDrunkStates() {
        try {
            persistenceHandler.retrieveAll(drunkStateDataType)
                    .forEach(pair -> drunks.put(pair.second(), pair.first()));
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    public @Nullable DrunkState consume(UUID playerUuid, int alcohol, int toxins) {
        return this.consume(playerUuid, alcohol, toxins, timeSupplier.getAsLong());
    }

    /**
     * @param playerUuid
     * @param alcohol
     * @param timestamp  Should be in relation to the internal clock in drunk manager
     */
    public @Nullable DrunkState consume(UUID playerUuid, int alcohol, int toxins, long timestamp) {
        boolean alreadyDrunk = drunks.containsKey(playerUuid);
        DrunkState drunkState = alreadyDrunk ?
                drunks.get(playerUuid).recalculate(timestamp).addAlcohol(alcohol, toxins) : new DrunkState(alcohol, toxins, 0, timestamp, -1);

        if (drunkState.alcohol() <= 0 && !isPassedOut(drunkState)) {
            drunks.remove(playerUuid);
            if (alreadyDrunk) {
                try {
                    persistenceHandler.remove(drunkStateDataType, playerUuid);
                } catch (PersistenceException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        drunks.put(playerUuid, drunkState);
        planEvent(playerUuid);
        try {
            if (alreadyDrunk) {
                persistenceHandler.updateValue(drunkStateDataType, new Pair<>(drunkState, playerUuid));
            } else {
                persistenceHandler.insertValue(drunkStateDataType, new Pair<>(drunkState, playerUuid));
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return drunkState;
    }

    public @Nullable DrunkState getDrunkState(UUID playerUuid) {
        boolean alreadyDrunk = drunks.containsKey(playerUuid);
        return alreadyDrunk ?
                drunks.get(playerUuid).recalculate(timeSupplier.getAsLong()) : null;

    }

    public void reset(Set<BreweryKey> allowedEvents) {
        plannedEvents.clear();
        drunks.clear();
        this.allowedEvents = allowedEvents;
        events.clear();
        loadDrunkStates();
    }

    public void clear(UUID playerUuid) {
        Long plannedEventTime = plannedEvents.remove(playerUuid);
        drunks.remove(playerUuid);
        if (plannedEventTime == null) {
            return;
        }
        if (events.containsKey(plannedEventTime)) {
            events.get(plannedEventTime).remove(playerUuid);
        }
    }

    public void tick(BiConsumer<UUID, DrunkEvent> action) {
        Map<UUID, DrunkEvent> currentEvents = events.remove(timeSupplier.getAsLong());
        if (currentEvents == null) {
            return;
        }
        Set<UUID> toRemove = new HashSet<>();
        for (UUID currentEvent : currentEvents.keySet()) {
            if (!drunks.containsKey(currentEvent)) {
                toRemove.add(currentEvent);
            }
        }
        currentEvents.forEach((key, value) -> plannedEvents.remove(key));
        toRemove.forEach(currentEvents::remove);
        currentEvents.forEach(action);
        currentEvents.forEach((playerUuid, event) -> planEvent(playerUuid));
    }

    public void planEvent(UUID playerUuid) {
        if (plannedEvents.containsKey(playerUuid)) {
            return;
        }
        DrunkState drunkState = getDrunkState(playerUuid);
        if (drunkState == null) {
            return;
        }
        List<DrunkEvent> drunkEvents = Stream.concat(
                        Registry.DRUNK_EVENT.values().stream(),
                        eventRegistry.events().stream())
                .filter(event -> allowedEvents.contains(event.key()))
                .filter(drunkEvent -> drunkEvent.alcoholRequirement() <= drunkState.alcohol())
                .filter(drunkEvent -> drunkEvent.toxinsRequirement() <= drunkState.toxins())
                .filter(drunkEvent -> drunkEvent.probabilityWeight() > 0)
                .toList();
        if (drunkEvents.isEmpty()) {
            return;
        }
        int cumulativeSum = RandomUtil.cumulativeSum(drunkEvents);
        DrunkEvent drunkEvent = RandomUtil.randomWeighted(drunkEvents);
        double value = (double) 10000 * (125 - drunkState.alcohol()) / cumulativeSum / 25;
        long time = (long) (timeSupplier.getAsLong() + Math.max(1, RANDOM.nextGaussian(value, value / 2)));
        events.computeIfAbsent(time, ignored -> new HashMap<>()).put(playerUuid, drunkEvent);
        plannedEvents.put(playerUuid, time);
    }

    public void registerPassedOut(@NotNull UUID playerUUID) {
        drunks.computeIfPresent(playerUUID, (ignored, drunkState) -> drunkState.withPassOut(timeSupplier.getAsLong()));
    }

    public boolean isPassedOut(@NotNull UUID playerUUID) {
        return drunks.containsKey(playerUUID) && isPassedOut(drunks.get(playerUUID));
    }

    private boolean isPassedOut(DrunkState drunkState) {
        return drunkState.kickedTimestamp() + (long) Config.PASS_OUT_TIME * Moment.MINUTE < timeSupplier.getAsLong();
    }

    public @Nullable Pair<DrunkEvent, Long> getPlannedEvent(UUID playerUUID) {
        Long time = plannedEvents.get(playerUUID);
        if (time == null) {
            return null;
        }
        return new Pair<>(events.get(time).get(playerUUID), time);
    }

    public void registerMovement(@NotNull UUID playerUUID, double speedSquared) {
        if (!drunks.containsKey(playerUUID)) {
            return;
        }
        drunks.computeIfPresent(playerUUID, (ignored, state) -> state.withSpeedSquared(speedSquared));
    }
}
