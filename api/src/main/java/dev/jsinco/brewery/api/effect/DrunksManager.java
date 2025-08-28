package dev.jsinco.brewery.api.effect;

import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface DrunksManager {

    /**
     * This is experimental, as there's plans to completely rewrite alcoholic traits
     * (alcohol, toxins)
     *
     * @param playerUuid A UUID of a player
     * @param alcohol    The alcohol amount to consume
     * @param toxins     The toxin amount to consume
     * @return The resulting drunken state after consuming the contents (Already saved internally)
     */
    @ApiStatus.Experimental
    @Nullable DrunkState consume(UUID playerUuid, int alcohol, int toxins);

    /**
     * @param playerUuid The UUID of a player
     * @return The drunken state of the player
     */
    @Nullable DrunkState getDrunkState(UUID playerUuid);

    /**
     * Completely reset this drunks manager, reloads from storage
     *
     * @param allowedEvents The events to use in random drunken events
     */
    void reset(@NotNull Set<BreweryKey> allowedEvents);

    /**
     * @param playerUuid A player UUID to clear all data on (persistent)
     */
    void clear(@NotNull UUID playerUuid);

    /**
     * Plan a random drunken event
     *
     * @param playerUuid A player UUID for the player to get a random drunken event for
     */
    void planEvent(@NotNull UUID playerUuid);

    /**
     * Register a player as passed out, i.e. kick on join (persistent)
     *
     * @param playerUuid A UUID of a player
     */
    void registerPassedOut(@NotNull UUID playerUuid);

    /**
     * @param playerUUID A UUID of a player
     * @return True if the player is passed out
     */
    boolean isPassedOut(@NotNull UUID playerUUID);

    /**
     * @param playerUUID A UUID of a player
     * @return A pair with a drunken event, and the plugin time to run it
     */
    @Nullable Pair<DrunkEvent, Long> getPlannedEvent(@NotNull UUID playerUUID);
}
