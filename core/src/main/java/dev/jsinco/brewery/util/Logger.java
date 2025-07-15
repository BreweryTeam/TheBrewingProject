package dev.jsinco.brewery.util;

import java.util.logging.Level;

public final class Logger {

    public static void log(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[TBP Debug - " + className + ":" + caller.getLineNumber() + "] " + message;
        java.util.logging.Logger.getLogger("TheBrewingProject").log(Level.WARNING, prefixedMessage);
    }

    public static void logErr(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[TBP Error - " + className + ":" + caller.getLineNumber() + "] " + message;
        java.util.logging.Logger.getLogger("TheBrewingProject").log(Level.SEVERE, prefixedMessage);
    }

    public static void logErr(Throwable throwable) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefix = "[TBP Error - " + className + ":" + caller.getLineNumber() + "] ";
        java.util.logging.Logger.getLogger("TheBrewingProject").log(Level.SEVERE, prefix + throwable.getMessage(), throwable);
    }

    public static void logDev(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[TBP DevDebug - " + className + ":" + caller.getLineNumber() + "] " + message;
        java.util.logging.Logger.getLogger("TheBrewingProject").log(Level.WARNING, prefixedMessage);
    }

}
