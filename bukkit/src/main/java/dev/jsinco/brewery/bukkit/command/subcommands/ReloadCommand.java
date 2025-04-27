package dev.jsinco.brewery.bukkit.command.subcommands;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.command.BukkitSubCommand;
import dev.jsinco.brewery.command.SubCommandInfo;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

@SubCommandInfo(
        name = "reload",
        permission = "brewery.command.reload"
)
public class ReloadCommand implements BukkitSubCommand {
    @Override
    public void execute(TheBrewingProject instance, CommandSender sender, OfflinePlayer target, String label, List<String> args) {
        TheBrewingProject.getInstance().reload();
        sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_RELOAD_MESSAGE));
    }

    @Override
    public List<String> tabComplete(TheBrewingProject instance, CommandSender sender, OfflinePlayer target, String label, List<String> args) {
        return List.of();
    }
}
