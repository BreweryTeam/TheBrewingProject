package dev.jsinco.brewery.effect;

import dev.jsinco.brewery.configuration.Config;

public record DrunkState(int alcohol, int toxins, double walkSpeedSquared, long timestamp, long kickedTimestamp) {

    public DrunkState recalculate(long timestamp) {
        if (timestamp < this.timestamp) {
            return new DrunkState(this.alcohol, this.toxins, this.walkSpeedSquared, this.timestamp, this.kickedTimestamp);
        }
        int diff = (int) (timestamp - this.timestamp);
        // Assume that the drunk value does not get recalculated that much
        int alcohol = (this.alcohol - diff / Config.ALCOHOL_DECAY_RATE);
        int toxins = (this.toxins - diff / Config.TOXIN_DECAY_RATE);
        return new DrunkState(Math.max(0, Math.min(alcohol, 100)), Math.max(0, Math.min(toxins, 100)), this.walkSpeedSquared, timestamp, this.kickedTimestamp);
    }

    public DrunkState addAlcohol(int alcohol, int toxins) {
        return new DrunkState(
                Math.max(0, Math.min(this.alcohol + alcohol, 100)),
                Math.min(this.toxins + toxins, 100),
                this.walkSpeedSquared,
                timestamp,
                this.kickedTimestamp
        );
    }

    public DrunkState withSpeedSquared(double speedSquared) {
        return new DrunkState(
                this.alcohol, this.toxins, speedSquared, this.timestamp, this.kickedTimestamp
        );
    }

    public DrunkState withPassOut(long kickedTimestamp) {
        return new DrunkState(this.alcohol, this.toxins, this.walkSpeedSquared, this.timestamp, kickedTimestamp);
    }
}
