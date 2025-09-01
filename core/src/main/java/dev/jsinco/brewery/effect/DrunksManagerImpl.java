package dev.jsinco.brewery.effect;

import com.google.common.collect.ImmutableList;
import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.DrunksManager;
import dev.jsinco.brewery.api.effect.ModifierConsume;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.CustomEventRegistry;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.PersistenceHandler;
import dev.jsinco.brewery.util.RandomUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DrunksManagerImpl<C> implements DrunksManager {

    private final CustomEventRegistry eventRegistry;
    private final PersistenceHandler<C> persistenceHandler;
    private final DrunkStateDataType<C> drunkStateDataType;
    private final DrunkenModifierDataType<C> drunkenModifierDataType;
    private Set<BreweryKey> allowedEvents;
    private List<NamedDrunkEvent> namedDrunkEvents = initializeDrunkEventsWithOverrides();
    private Map<UUID, DrunkState> drunks = new HashMap<>();
    @Getter
    private LongSupplier timeSupplier;
    private Map<Long, Map<UUID, DrunkEvent>> events = new HashMap<>();
    private Map<UUID, Long> plannedEvents = new HashMap<>();

    private static final Random RANDOM = new Random();

    public DrunksManagerImpl(CustomEventRegistry registry, Set<BreweryKey> allowedEvents, LongSupplier timeSupplier,
                             PersistenceHandler<C> persistenceHandler, DrunkStateDataType<C> drunkStateDataType, DrunkenModifierDataType<C> drunkenModifierDataType) {
        this.eventRegistry = registry;
        this.allowedEvents = allowedEvents;
        this.timeSupplier = timeSupplier;
        this.persistenceHandler = persistenceHandler;
        this.drunkStateDataType = drunkStateDataType;
        this.drunkenModifierDataType = drunkenModifierDataType;
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

    @Override
    public @Nullable DrunkState consume(UUID playerUuid, String modifierName, double value) {
        return consume(playerUuid,
                new ModifierConsume(
                        DrunkenModifierSection.modifiers().modifier(modifierName),
                        value
                ));
    }

    public @Nullable DrunkState consume(UUID playerUuid, List<ModifierConsume> consumptions) {
        return this.consume(playerUuid, consumptions, timeSupplier.getAsLong());
    }

    @Override
    public @Nullable DrunkState consume(UUID playerUuid, ModifierConsume modifier) {
        return this.consume(playerUuid, List.of(modifier));
    }

    /**
     * @param timestamp Should be in relation to the internal clock in drunk manager
     */
    public @Nullable DrunkState consume(UUID playerUuid, List<ModifierConsume> modifiers, long timestamp) {
        boolean alreadyDrunk = drunks.containsKey(playerUuid);
        DrunkState initialState = (alreadyDrunk ? drunks.get(playerUuid).recalculate(timestamp) : new DrunkStateImpl(
                timestamp, -1, DrunkenModifierSection.modifiers()
                .drunkenModifiers().stream()
                .collect(Collectors.toUnmodifiableMap(temp -> temp, DrunkenModifier::defaultValue))
        ));
        DrunkState newState = initialState;
        // Behave exactly the same when a modifier is changing
        List<ModifierConsume> sortedModifiers = new ArrayList<>(modifiers);
        sortedModifiers.sort(
                Comparator.comparing(modifierConsume -> modifierConsume.modifier().name(), String::compareTo)
        );
        for (ModifierConsume modifierConsume : sortedModifiers) {
            if (modifierConsume.cascade()) {
                Pair<DrunkState, Boolean> drunkStateChange = newState.cascadeModifier(modifierConsume.modifier(), modifierConsume.value());
                newState = drunkStateChange.first();
                if (drunkStateChange.second()) {
                    continue;
                }
            }
            newState = newState.addModifier(modifierConsume.modifier(), modifierConsume.value());

        }
        if (newState.additionalModifierData().isEmpty() && !isPassedOut(newState)) {
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
        drunks.put(playerUuid, newState);
        planEvent(playerUuid);
        try {
            if (alreadyDrunk) {
                persistenceHandler.updateValue(drunkStateDataType, new Pair<>(newState, playerUuid));
            } else {
                persistenceHandler.insertValue(drunkStateDataType, new Pair<>(newState, playerUuid));
            }
            Set<DrunkenModifier> allModifiers = Stream.concat(initialState.additionalModifierData().stream(), newState.additionalModifierData().stream())
                    .map(Pair::first)
                    .collect(Collectors.toSet());
            Map<DrunkenModifier, Double> newModifiers = newState.modifiers();
            for (DrunkenModifier modifier : allModifiers) {
                if (newModifiers.get(modifier) != modifier.defaultValue()) {
                    persistenceHandler.insertValue(drunkenModifierDataType, new Pair<>(new DrunkenModifierDataType.Data(modifier, playerUuid), newModifiers.get(modifier)));
                }
                if (newModifiers.get(modifier) == modifier.defaultValue()) {
                    persistenceHandler.remove(drunkenModifierDataType, new DrunkenModifierDataType.Data(modifier, playerUuid));
                }
            }
        } catch (PersistenceException e) {
            Logger.logErr(e);
        }
        return newState;
    }

    @Override
    public @Nullable DrunkState getDrunkState(UUID playerUuid) {
        boolean alreadyDrunk = drunks.containsKey(playerUuid);
        return Optional.ofNullable(alreadyDrunk ? drunks.get(playerUuid).recalculate(timeSupplier.getAsLong()) : null)
                .filter(drunkState -> !drunkState.additionalModifierData().isEmpty())
                .orElse(null);

    }

    @Override
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

    @Override
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

    @Override
    public void planEvent(@NotNull UUID playerUuid) {
        if (plannedEvents.containsKey(playerUuid)) {
            return;
        }
        DrunkState drunkState = getDrunkState(playerUuid);
        if (drunkState == null) {
            return;
        }
        List<Pair<DrunkEvent, Double>> drunkEvents = Stream.concat(
                        namedDrunkEvents.stream(),
                        eventRegistry.events().stream())
                .filter(event -> allowedEvents.contains(event.key()))
                .map(drunkEvent -> new Pair<>(drunkEvent, drunkEvent.probability().evaluate(DrunkStateImpl.compileVariables(drunkState.modifiers(), null, 0D))))
                .filter(drunkEvent -> drunkEvent.second().enabled())
                .map(drunkEvent -> new Pair<>(drunkEvent.first(), drunkEvent.second().probability()))
                .filter(drunkEvent -> drunkEvent.second() > 0)
                .toList();
        if (drunkEvents.isEmpty()) {
            return;
        }
        double cumulativeSum = drunkEvents.stream()
                .map(Pair::second)
                .reduce(0D, Double::sum);
        DrunkEvent drunkEvent = RandomUtil.randomWeighted(drunkEvents, Pair::second).first();
        double value = (double) 500 / cumulativeSum;
        long time = (long) (timeSupplier.getAsLong() + Math.max(1, RANDOM.nextGaussian(value, value / 2)));
        events.computeIfAbsent(time, ignored -> new HashMap<>()).put(playerUuid, drunkEvent);
        plannedEvents.put(playerUuid, time);
    }

    @Override
    public void registerPassedOut(@NotNull UUID playerUuid) {
        drunks.computeIfPresent(playerUuid, (ignored, drunkState) -> drunkState.withPassOut(timeSupplier.getAsLong()));
    }

    @Override
    public boolean isPassedOut(@NotNull UUID playerUUID) {
        return drunks.containsKey(playerUUID) && isPassedOut(drunks.get(playerUUID));
    }

    private boolean isPassedOut(DrunkState drunkState) {
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
