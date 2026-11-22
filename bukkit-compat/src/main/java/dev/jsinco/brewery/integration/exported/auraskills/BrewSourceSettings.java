package dev.jsinco.brewery.integration.exported.auraskills;

import dev.aurelium.auraskills.api.config.ConfigNode;
import dev.aurelium.auraskills.api.source.CustomSource;
import dev.aurelium.auraskills.api.source.SourceContext;
import dev.aurelium.auraskills.api.source.SourceValues;

public class BrewSourceSettings extends CustomSource {

    private final CompletionMode mode;
    private final double multiplier;

    public BrewSourceSettings(SourceValues values, CompletionMode mode, double multiplier) {
        super(values);
        this.mode = mode;
        this.multiplier = multiplier;
    }

    public static BrewSourceSettings from(ConfigNode source, SourceContext context) {
        CompletionMode mode = source.node("mode").get(CompletionMode.class, CompletionMode.INCOMPLETE);
        double multiplier = source.node("multiplier").getDouble(1.0);
        return new BrewSourceSettings(context.parseValues(source), mode, multiplier);
    }

    public CompletionMode mode() {
        return mode;
    }

    public double multiplier() {
        return multiplier;
    }
}
