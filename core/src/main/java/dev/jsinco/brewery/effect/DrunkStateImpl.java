package dev.jsinco.brewery.effect;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.DecayRateSection;

public record DrunkStateImpl(int alcohol, int toxins, long timestamp,
                             long kickedTimestamp) implements DrunkState {

    public DrunkStateImpl recalculate(long timestamp) {
        if (timestamp < this.timestamp) {
            return new DrunkStateImpl(this.alcohol, this.toxins, this.timestamp, this.kickedTimestamp);
        }
        int diff = (int) (timestamp - this.timestamp);
        // Assume that the drunk value does not get recalculated that much
        DecayRateSection decayRateSection = Config.config().decayRate();
        int alcohol = (this.alcohol - diff / decayRateSection.alcohol());
        int toxins = (this.toxins - diff / decayRateSection.toxin());
        return new DrunkStateImpl(Math.max(0, Math.min(alcohol, 100)), Math.max(0, Math.min(toxins, 100)), timestamp, this.kickedTimestamp);
    }

    public DrunkStateImpl addAlcohol(int alcohol, int toxins) {
        return new DrunkStateImpl(
                Math.max(0, Math.min(this.alcohol + alcohol, 100)),
                Math.min(this.toxins + toxins, 100),
                this.timestamp,
                this.kickedTimestamp
        );
    }

    public DrunkStateImpl withSpeedSquared(double speedSquared) {
        return new DrunkStateImpl(
                this.alcohol, this.toxins, this.timestamp, this.kickedTimestamp
        );
    }

    public DrunkStateImpl withPassOut(long kickedTimestamp) {
        return new DrunkStateImpl(this.alcohol, this.toxins, this.timestamp, kickedTimestamp);
    }
}
