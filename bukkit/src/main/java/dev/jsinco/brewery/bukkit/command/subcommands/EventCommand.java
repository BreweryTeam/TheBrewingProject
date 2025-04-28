package dev.jsinco.brewery.bukkit.command.subcommands;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.command.BukkitSubCommand;
import dev.jsinco.brewery.bukkit.effect.event.NamedDrunkEventExecutor;
import dev.jsinco.brewery.command.SubCommandInfo;
import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Registry;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SubCommandInfo(
        name = "event",
        permission = "brewery.command.event"
)
public class EventCommand implements BukkitSubCommand {
    @Override
    public void execute(TheBrewingProject instance, CommandSender sender, OfflinePlayer target, String label, List<String> args) {
        NamedDrunkEvent namedDrunkEvent = Registry.DRUNK_EVENT.get(BreweryKey.parse(args.getFirst()));
        if (namedDrunkEvent != null) {
            NamedDrunkEventExecutor.doDrunkEvent(target.getUniqueId(), namedDrunkEvent);
            return;
        }
        TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvent(target.getUniqueId(), TheBrewingProject.getInstance().getCustomDrunkEventRegistry().getCustomEvent(BreweryKey.parse(args.getFirst())));
    }

    @Override
    public List<String> tabComplete(TheBrewingProject instance, CommandSender sender, OfflinePlayer target, String label, List<String> args) {
        if (args.size() > 2) {
            return List.of();
        }
        return Stream.concat(Arrays.stream(NamedDrunkEvent.values()), TheBrewingProject.getInstance().getCustomDrunkEventRegistry().events().stream())
                .map(DrunkEvent::key)
                .map(BreweryKey::key)
                .toList();
    }
}
