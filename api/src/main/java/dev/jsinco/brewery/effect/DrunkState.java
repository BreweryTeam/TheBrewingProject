package dev.jsinco.brewery.effect;

public interface DrunkState {

    DrunkState recalculate(long timeStamp);

    DrunkState addAlcohol(int alcohol, int toxins);

    DrunkState withPassOut(long kickedTimestamp);

    int alcohol();

    int toxins();

    long timestamp();

    long kickedTimestamp();
}
