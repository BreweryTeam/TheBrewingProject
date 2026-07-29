package dev.jsinco.brewery.api.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

public class LoggingFilterHolder {

    private static LoggingFilter instance;

    @ApiStatus.Internal
    public static LoggingFilter instance() {
        if (instance == null) {
            instance = ServiceLoader.load(LoggingFilter.class, LoggingFilter.class.getClassLoader()).findFirst()
                    .orElseThrow();
        }
        return instance;
    }

}
