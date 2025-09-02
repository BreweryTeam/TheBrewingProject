package dev.jsinco.brewery.api.effect;

import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface DrunksManager {

    @Nullable DrunkState consume(UUID playerUuid, String modifierName, double value);

    @Nullable DrunkState consume(UUID playerUuid, List<ModifierConsume> consumptions);

    @Nullable DrunkState consume(UUID playerUuid, ModifierConsume modifier);

    @Nullable DrunkState getDrunkState(UUID playerUuid);

    void reset(Set<BreweryKey> allowedEvents);

    void clear(UUID playerUuid);

    void planEvent(UUID playerUuid);

    void registerPassedOut(@NotNull UUID playerUUID);

    boolean isPassedOut(@NotNull UUID playerUUID);

    @Nullable Pair<DrunkEvent, Long> getPlannedEvent(UUID playerUUID);
}
