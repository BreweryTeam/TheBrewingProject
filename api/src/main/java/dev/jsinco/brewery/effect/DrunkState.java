package dev.jsinco.brewery.effect;

import org.jetbrains.annotations.ApiStatus;

public interface DrunkState {

    /**
     * Recalculate the state for a new timestamp
     *
     * @param timeStamp The new timestamp
     * @return A new drunk state instance recalculated for this timestamp
     */
    DrunkState recalculate(long timeStamp);

    /**
     * Alcoholic traits are planned to change
     *
     * @param alcohol Alcohol level
     * @param toxins  Toxin level
     * @return A new drunk state instance with the new alcohol levels
     */
    @ApiStatus.Experimental
    DrunkState addAlcohol(int alcohol, int toxins);

    /**
     * @param kickedTimestamp The time the player got kicked
     * @return A new drunk state with a new kicked timestamp
     */
    DrunkState withPassOut(long kickedTimestamp);

    /**
     * Alcoholic traits are planned to change
     *
     * @return The alcohol levels for this drunken state
     */
    @ApiStatus.Experimental
    int alcohol();

    /**
     * Alcoholic traits are planned to change
     *
     * @return The toxin levels for this drunken state
     */
    @ApiStatus.Experimental
    int toxins();

    /**
     * A timestamp is used, because of that the state does not need to be updated every tick
     *
     * @return The time stamp for this drunken state
     */
    long timestamp();

    /**
     * @return The timestamp the player was kicked
     */
    long kickedTimestamp();
}
