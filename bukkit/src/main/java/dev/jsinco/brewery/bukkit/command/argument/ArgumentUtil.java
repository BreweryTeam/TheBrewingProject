package dev.jsinco.brewery.bukkit.command.argument;

import java.util.regex.Pattern;

public class ArgumentUtil {
    private static final Pattern WORD_ARGUMENT = Pattern.compile("[a-zA-Z0-9+\\-_.]+");

    private ArgumentUtil(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static String sanitizeName(String name) {
        if (WORD_ARGUMENT.matcher(name).matches()) {
            return name;
        }
        return "\"" + name + "\"";
    }

    public static String escapeQuotes(String initial) {
        return initial.startsWith("\"") ? initial.substring(1) : initial;
    }
}
