package dev.jsinco.brewery.format;

public enum TimeFormat {

    CLOCK_MECHANIC("clock-mechanic"),
    COOKING_TIME("cooking-time"),
    MIXING_TIME("mixing-time"),
    AGING_YEARS("aging-years");

    private final String key;

    TimeFormat(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String get() {
        return TimeFormatRegistry.get(this);
    }
}
