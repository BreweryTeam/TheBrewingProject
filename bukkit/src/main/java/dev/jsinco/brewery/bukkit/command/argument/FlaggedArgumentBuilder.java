package dev.jsinco.brewery.bukkit.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jsinco.brewery.util.Pair;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class FlaggedArgumentBuilder {

    private final Set<Flag> flags;
    private final BiCommandContext<List<Flag>> onCommand;
    private final boolean hasMandatoryFirst;

    public FlaggedArgumentBuilder(Set<Flag> flags, BiCommandContext<List<Flag>> onCommand) {
        this.flags = flags;
        this.onCommand = onCommand;
        this.hasMandatoryFirst = flags.stream().anyMatch(flag -> flag.flagProperties.contains(FlagProperty.MANDATORY_FIRST));
    }

    public List<ArgumentBuilder<CommandSourceStack, ?>> build() {
        return build(flags, 0);
    }

    private List<ArgumentBuilder<CommandSourceStack, ?>> build(Set<Flag> flags, int depth) {
        List<ArgumentBuilder<CommandSourceStack, ?>> output = new ArrayList<>();
        List<Flag> flagsToUse = flags.stream().filter(flag -> filterFlag(flag, depth)).toList();
        for (Flag flag : flagsToUse) {
            Set<Flag> reducedFlags = new HashSet<>(flags);
            reducedFlags.remove(flag);
            ArgumentBuilder<CommandSourceStack, ?> fullFlagArgument = Commands.literal("--" + flag.fullName());
            List<ArgumentBuilder<CommandSourceStack, ?>> flagArguments = build(reducedFlags, depth + 1);
            stack(flag.flagArguments(), (argument) -> {
                argument.executes(this::execute);
                flagArguments.forEach(argument::then);
            }).ifPresentOrElse(fullFlagArgument::then, () -> flagArguments.forEach(fullFlagArgument::then));
            output.add(fullFlagArgument);
            if (flag.shortName() != null) {
                ArgumentBuilder<CommandSourceStack, ?> shortFlagArgument = Commands.literal("-" + flag.shortName());
                stack(flag.flagArguments(), (argument) -> {
                    argument.executes(this::execute);
                    flagArguments.forEach(argument::then);
                }).ifPresentOrElse(shortFlagArgument::then, () -> flagArguments.forEach(shortFlagArgument::then));
                output.add(shortFlagArgument);
            }
        }
        return output;
    }

    private boolean filterFlag(Flag flag, int depth) {
        if (depth == 0 && hasMandatoryFirst) {
            return flag.flagProperties().contains(FlagProperty.MANDATORY_FIRST);
        } else {
            return !flag.flagProperties().contains(FlagProperty.ONLY_FIRST);
        }
    }

    private int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String[] arguments = context.getInput().split("\\s+");
        List<Flag> flagsList = new ArrayList<>();
        for (String argument : arguments) {
            findFlag(argument).ifPresent(flagsList::add);
        }
        onCommand.accept(context, flagsList);
        return 1;
    }

    private Optional<Flag> findFlag(String argument) {
        if (!argument.startsWith("-")) {
            return Optional.empty();
        }
        String noPrefixedFlag = argument.replace("-", "");
        for (Flag flag : flags) {
            if (flag.fullName().equals(noPrefixedFlag)) {
                return Optional.of(flag);
            }
            if (noPrefixedFlag.equals(flag.shortName())) {
                return Optional.of(flag);
            }
        }
        return Optional.empty();
    }

    private Optional<ArgumentBuilder<CommandSourceStack, ?>> stack(List<Pair<String, ArgumentType<?>>> sequence, Consumer<ArgumentBuilder<CommandSourceStack, ?>> lastArgumentConsumer) {
        List<Pair<String, ArgumentType<?>>> reversed = sequence.reversed();
        Iterator<? extends RequiredArgumentBuilder<CommandSourceStack, ?>> iterator = reversed.stream().map(argument ->
                Commands.argument(argument.first(), argument.second())
        ).iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        ArgumentBuilder<CommandSourceStack, ?> argumentBuilder = iterator.next();
        ArgumentBuilder<CommandSourceStack, ?> lastArgument = argumentBuilder;
        lastArgumentConsumer.accept(lastArgument);
        while (iterator.hasNext()) {
            ArgumentBuilder<CommandSourceStack, ?> next = iterator.next();
            next.then(argumentBuilder);
            argumentBuilder = next;
        }
        return Optional.of(argumentBuilder);
    }

    public record Flag(String fullName, @Nullable String shortName,
                       List<Pair<String, ArgumentType<?>>> flagArguments, Set<FlagProperty> flagProperties) {

    }

    public enum FlagProperty {
        MANDATORY_FIRST,
        ONLY_FIRST
    }
}
