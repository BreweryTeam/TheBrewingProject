package dev.jsinco.brewery.bukkit.command.subcommands;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.command.BukkitSubCommand;
import dev.jsinco.brewery.command.SubCommandInfo;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Optional;

@SubCommandInfo(
        name = "seal",
        permission = "brewery.command.seal"
)
public class SealCommand implements BukkitSubCommand {

    @Override
    public void execute(TheBrewingProject instance, CommandSender sender, OfflinePlayer offlineTarget, String label, List<String> args) {
        Player target = toOnlineTarget(offlineTarget);
        if (target == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_UNDEFINED_PLAYER));
            return;
        }

        PlayerInventory targetInventory = target.getInventory();
        boolean sealAll = !args.isEmpty() && args.getFirst().equals("all");
        if (sealAll) {
            args = args.subList(1, args.size());
        }
        Component volumeMessage = !args.isEmpty() ? LegacyComponentSerializer.legacyAmpersand().deserialize(
                String.join(" ", args)
        ) : null;
        String serializedVolumeMessage = volumeMessage != null ? MiniMessage.miniMessage().serialize(volumeMessage) : null;
        if (!sealAll) {
            BrewAdapter.fromItem(targetInventory.getItemInMainHand())
                    .map(brew -> BrewAdapter.toItem(brew, new BrewImpl.State.Seal(serializedVolumeMessage)))
                    .ifPresentOrElse(itemStack -> {
                        targetInventory.setItemInMainHand(itemStack);
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_SEAL_SUCCESS, Placeholder.unparsed("player_name", target.getName())));
                    }, () -> {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_SEAL_FAILURE));
                    });
        } else {
            boolean oneFound = false;
            for (int i = 0; i < targetInventory.getSize(); i++) {
                ItemStack itemStack = targetInventory.getItem(i);
                if (itemStack == null) {
                    continue;
                }
                Optional<Brew> brewOptional = BrewAdapter.fromItem(itemStack);
                if (brewOptional.isPresent()) {
                    oneFound = true;
                    targetInventory.setItem(i, BrewAdapter.toItem(brewOptional.get(), new BrewImpl.State.Seal(serializedVolumeMessage)));
                }
            }
            if (oneFound) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_SEAL_SUCCESS, Placeholder.unparsed("player_name", target.getName())));
            } else {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_SEAL_FAILURE));
            }
        }
    }

    @Override
    public List<String> tabComplete(TheBrewingProject instance, CommandSender sender, OfflinePlayer target, String label, List<String> args) {
        if (args.size() == 2) {
            return List.of("<volume-info>", "all");
        }
        return List.of();
    }
}
