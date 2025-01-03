package dev.jsinco.brewery.structure;

import org.bukkit.Bukkit;

import java.util.Collection;

public class SubstitutionUtils {

    private SubstitutionUtils() {
        throw new IllegalStateException();
    }

    public static String substituteWithTarget(String initial, String target, Collection<String> patterns) {
        String output = initial;
        for (String pattern : patterns) {
            output = output.replace(pattern, target);
        }
        return output;
    }
}
