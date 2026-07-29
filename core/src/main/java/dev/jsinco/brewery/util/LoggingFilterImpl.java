package dev.jsinco.brewery.util;

import dev.jsinco.brewery.api.util.LoggingFilter;
import dev.jsinco.brewery.api.util.LoggingModule;
import dev.jsinco.brewery.configuration.Config;

public class LoggingFilterImpl implements LoggingFilter {
    
    @Override
    public boolean test(LoggingModule module) {
        return Config.config().verboseLogging().contains(module);
    }
}
