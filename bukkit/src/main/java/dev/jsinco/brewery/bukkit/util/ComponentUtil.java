package dev.jsinco.brewery.bukkit.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class ComponentUtil {

    /**
     * Splits a Component into a List<Component> with one Component per line
     * (I have no idea how I did that, but it works and that's what matters)
     */
    public static List<Component> splitIntoLines(Component component) {
        List<Component> lines = new ArrayList<>();
        List<Component> currentLine = new ArrayList<>();
        splitRecursive(component, currentLine, lines);
        if (!currentLine.isEmpty()) {
            lines.add(Component.empty().children(currentLine));
        }
        return lines;
    }

    private static void splitRecursive(Component component, List<Component> currentLine, List<Component> lines) {
        if (component instanceof TextComponent text) {
            String[] parts = text.content().split("\\n", -1);
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    currentLine.add(Component.text(parts[i], text.style()));
                }
                if (i < parts.length - 1) {
                    lines.add(Component.empty().children(currentLine));
                    currentLine.clear();
                }
            }
        } else if (component.equals(Component.newline())) {
            lines.add(Component.empty().children(currentLine));
            currentLine.clear();
        } else {
            currentLine.add(component);
        }
        for (Component child : component.children()) {
            splitRecursive(child, currentLine, lines);
        }
    }
}
