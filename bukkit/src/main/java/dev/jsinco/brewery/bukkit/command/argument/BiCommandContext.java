package dev.jsinco.brewery.bukkit.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface BiCommandContext<T> {

    void accept(CommandContext<CommandSourceStack> context, T extraPayload) throws CommandSyntaxException;
}
