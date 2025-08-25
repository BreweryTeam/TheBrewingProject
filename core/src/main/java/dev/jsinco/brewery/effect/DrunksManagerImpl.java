package dev.jsinco.brewery.effect;

import com.google.common.collect.ImmutableList;
import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.PersistenceHandler;
import dev.jsinco.brewery.event.CustomEventRegistry;
import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DrunksManagerImpl<C> implements DrunksManager {

    private final CustomEventRegistry eventRegistry;
    private final PersistenceHandler<C> persistenceHandler;
    private final DrunkStateDataType<C> drunkStateDataType;
    private Set<BreweryKey> allowedEvents;
    private List<NamedDrunkEvent> namedDrunkEvents = initializeDrunkEventsWithOverrides();
    private Map<UUID, DrunkStateImpl> drunks = new HashMap<>();
    @Getter
    private LongSupplier timeSupplier;
    private Map<Long, Map<UUID, DrunkEvent>> events = new HashMap<>();
    private Map<UUID, Long> plannedEvents = new HashMap<>();

    private static final Random RANDOM = new Random();

    public DrunksManagerImpl(CustomEventRegistry registry, Set<BreweryKey> allowedEvents, LongSupplier timeSupplier,
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
            persistenceHandler.retrieveAllNow(drunkStateDataType)
                    .forEach(pair -> drunks.put(pair.second(), pair.first()));
        } catch (PersistenceException e) {
            Logger.logErr(e);
        }
    }

    public @Nullable DrunkStateImpl consume(UUID playerUuid, int alcohol, int toxins) {
        return this.consume(playerUuid, alcohol, toxins, timeSupplier.getAsLong());
    }

    /**
     * @param playerUuid
     * @param alcohol
     * @param timestamp  Should be in relation to the internal clock in drunk manager
     */
    public @Nullable DrunkStateImpl consume(UUID playerUuid, int alcohol, int toxins, long timestamp) {
        boolean alreadyDrunk = drunks.containsKey(playerUuid);
        DrunkStateImpl drunkState = alreadyDrunk ?
                drunks.get(playerUuid).recalculate(timestamp).addAlcohol(alcohol, toxins) : new DrunkStateImpl(alcohol, toxins, timestamp, -1);

        if (drunkState.alcohol() <= 0 && !isPassedOut(drunkState)) {
            drunks.remove(playerUuid);
            if (alreadyDrunk) {
                try {
                    persistenceHandler.remove(drunkStateDataType, playerUuid);
                } catch (PersistenceException e) {
                    Logger.logErr(e);
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
            Logger.logErr(e);
        }
        return drunkState;
    }

    public @Nullable DrunkStateImpl getDrunkState(UUID playerUuid) {
        boolean alreadyDrunk = drunks.containsKey(playerUuid);
        return alreadyDrunk ?
                drunks.get(playerUuid).recalculate(timeSupplier.getAsLong()) : null;

    }

    public void reset(@NotNull Set<BreweryKey> allowedEvents) {
        plannedEvents.clear();
        drunks.clear();
        this.allowedEvents = allowedEvents;
        events.clear();
        loadDrunkStates();
        drunks.keySet().forEach(this::planEvent);
        namedDrunkEvents = initializeDrunkEventsWithOverrides();
    }

    private List<NamedDrunkEvent> initializeDrunkEventsWithOverrides() {
        ImmutableList.Builder<NamedDrunkEvent> output = new ImmutableList.Builder<>();
        for (NamedDrunkEvent namedDrunkEvent : BreweryRegistry.DRUNK_EVENT.values()) {
            EventSection.events().namedDrunkEventsOverride()
                    .stream()
                    .filter(namedDrunkEvent::equals)
                    .findAny()
                    .ifPresentOrElse(output::add, () -> output.add(namedDrunkEvent));
        }
        return output.build();
    }

    public void clear(@NotNull UUID playerUuid) {
        Long plannedEventTime = plannedEvents.remove(playerUuid);
        drunks.remove(playerUuid);
        try {
            persistenceHandler.remove(drunkStateDataType, playerUuid);
        } catch (PersistenceException e) {
            Logger.logErr(e);
        }
        if (plannedEventTime == null) {
            return;
        }
        if (events.containsKey(plannedEventTime)) {
            events.get(plannedEventTime).remove(playerUuid);
        }
    }

    public void tick(BiConsumer<UUID, DrunkEvent> action, Predicate<UUID> onlinePredicate) {
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
        currentEvents.keySet()
                .stream()
                .filter(onlinePredicate)
                .forEach(this::planEvent);
    }

    public void planEvent(@NotNull UUID playerUuid) {
        if (plannedEvents.containsKey(playerUuid)) {
            return;
        }
        DrunkStateImpl drunkState = getDrunkState(playerUuid);
        if (drunkState == null) {
            return;
        }
        List<DrunkEvent> drunkEvents = Stream.concat(
                        namedDrunkEvents.stream(),
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
        double value = (double) 500 * (110 - drunkState.alcohol()) / cumulativeSum;
        long time = (long) (timeSupplier.getAsLong() + Math.max(1, RANDOM.nextGaussian(value, value / 2)));
        events.computeIfAbsent(time, ignored -> new HashMap<>()).put(playerUuid, drunkEvent);
        plannedEvents.put(playerUuid, time);
    }

    public void registerPassedOut(@NotNull UUID playerUuid) {
        drunks.computeIfPresent(playerUuid, (ignored, drunkState) -> drunkState.withPassOut(timeSupplier.getAsLong()));
    }

    public boolean isPassedOut(@NotNull UUID playerUUID) {
        return drunks.containsKey(playerUUID) && isPassedOut(drunks.get(playerUUID));
    }

    private boolean isPassedOut(DrunkStateImpl drunkState) {
        long passOutTimeStamp = drunkState.kickedTimestamp();
        if (passOutTimeStamp == -1) {
            return false;
        }
        return passOutTimeStamp + (long) EventSection.events().passOutTime() * Moment.MINUTE > timeSupplier.getAsLong();
    }

    @Override
    public @Nullable Pair<DrunkEvent, Long> getPlannedEvent(@NotNull UUID playerUUID) {
        Long time = plannedEvents.get(playerUUID);
        if (time == null) {
            return null;
        }
        return new Pair<>(events.get(time).get(playerUUID), time);
    }
}
