package dev.jsinco.brewery.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.logging.Level;

@ApiStatus.Internal
public final class Logger {

    public static void log(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[TBP Info - " + className + ":" + caller.getLineNumber() + "] " + message;
        logger().log(Level.INFO, prefixedMessage);
    }

    public static void logErr(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[TBP Error - " + className + ":" + caller.getLineNumber() + "] " + message;
        logger().log(Level.SEVERE, prefixedMessage);
    }

    public static void logErr(Throwable throwable) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefix = "[TBP Error - " + className + ":" + caller.getLineNumber() + "] ";
        logger().log(Level.SEVERE, prefix + throwable.getMessage(), throwable);
    }

    public static void logDev(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[TBP DevDebug - " + className + ":" + caller.getLineNumber() + "] " + message;
        logger().log(Level.WARNING, prefixedMessage);
    }

    private static java.util.logging.Logger logger() {
        return java.util.logging.Logger.getLogger("TheBrewingProject");
    }

}
