package dev.jsinco.brewery.command;

import dev.jsinco.brewery.TheBrewingProjectApi;

import java.util.List;

/**
 * Represents an abstract sub-command where T is the main class of this mod or plugin.
 * @param <M> The main class of this mod or plugin.
 * @param <S> The sender type.
 * @param <T> The target type.
 */
public interface SubCommand<M extends TheBrewingProjectApi, S, T> {

    void execute(M instance, S sender, T target, String label, List<String> args);
    List<String> tabComplete(M instance, S sender, T target, String label, List<String> args);



    default SubCommandInfo commandInfo() {
        SubCommandInfo subCommandInfo = getClass().getAnnotation(SubCommandInfo.class);
        if (subCommandInfo == null) {
            throw new IllegalStateException("CommandInfo annotation is missing on SubCommand " + getClass().getName());
        }
        return subCommandInfo;
    }
}
