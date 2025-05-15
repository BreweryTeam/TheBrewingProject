package dev.jsinco.brewery.effect;

import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface DrunksManager {

    @Nullable DrunkState consume(UUID playerUuid, int alcohol, int toxins);

    @Nullable DrunkState getDrunkState(UUID playerUuid);

    void reset(Set<BreweryKey> allowedEvents);

    void clear(UUID playerUuid);

    void planEvent(UUID playerUuid);

    void registerPassedOut(@NotNull UUID playerUUID);

    boolean isPassedOut(@NotNull UUID playerUUID);

    @Nullable Pair<DrunkEvent, Long> getPlannedEvent(UUID playerUUID);
}
