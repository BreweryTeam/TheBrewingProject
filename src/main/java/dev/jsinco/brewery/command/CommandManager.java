package dev.jsinco.brewery.command;

public interface CommandManager<S> {
    boolean isPlayer(S sender);
    boolean hasPermission(S sender, String permission);
}
