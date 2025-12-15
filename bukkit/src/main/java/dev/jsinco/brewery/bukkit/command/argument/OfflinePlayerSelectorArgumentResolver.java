package dev.jsinco.brewery.bukkit.command.argument;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.OfflinePlayer;

public interface OfflinePlayerSelectorArgumentResolver {

    OfflinePlayer resolve(CommandSourceStack stack) throws CommandSyntaxException;
}
