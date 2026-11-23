package dev.jsinco.brewery.integration.hook.auraskills;

import dev.aurelium.auraskills.api.config.ConfigNode;
import dev.aurelium.auraskills.api.source.CustomSource;
import dev.aurelium.auraskills.api.source.SourceContext;
import dev.aurelium.auraskills.api.source.SourceValues;

public class BrewSourceSettings extends CustomSource {

    private final CompletionMode mode;

    public BrewSourceSettings(SourceValues values, CompletionMode mode) {
        super(values);
        this.mode = mode;
    }

    public static BrewSourceSettings from(ConfigNode source, SourceContext context) {
        CompletionMode mode = source.node("mode").get(CompletionMode.class, CompletionMode.INCOMPLETE);
        return new BrewSourceSettings(context.parseValues(source), mode);
    }

    public CompletionMode mode() {
        return mode;
    }
}
