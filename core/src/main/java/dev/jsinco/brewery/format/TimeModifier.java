package dev.jsinco.brewery.format;

import dev.jsinco.brewery.configuration.Config;

public enum TimeModifier {

    NORMAL(1200),
    COOKING(Config.config().cauldrons().cookingMinuteTicks()),
    AGING(Config.config().barrels().agingYearTicks() / (365 * 24 * 60));

    private final long ticksPerMinute;
    TimeModifier(long tpm) {
        this.ticksPerMinute = tpm;
    }

    public double getTicksPerMinute() {
        return ticksPerMinute;
    }
}
