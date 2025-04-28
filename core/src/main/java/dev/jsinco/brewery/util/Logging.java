package dev.jsinco.brewery.util;

import java.util.logging.Logger;

public final class Logging {

    public static void log(String message) {
        Logger.getLogger("TheBrewingProject").info(message);
    }

    public static void error(String message) {
        Logger.getLogger("TheBrewingProject").severe(message);
    }

    public static void warning(String message) {
        Logger.getLogger("TheBrewingProject").warning(message);
    }
}
