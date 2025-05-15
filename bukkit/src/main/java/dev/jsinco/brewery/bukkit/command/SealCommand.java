package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SealCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        ArgumentBuilder<CommandSourceStack, ?> sealAll = Commands.literal("all")
                .executes(SealCommand::sealAll)
                .then(Commands.argument("volume", StringArgumentType.greedyString())
                        .executes(SealCommand::sealAll));
        return Commands.literal("seal")
                .then(sealAll)
                .then(Commands.argument("volume", StringArgumentType.greedyString())
                        .executes(SealCommand::sealOne)
                ).then(BreweryCommand.playerBranch(argument -> {
                    argument.then(sealAll);
                    argument.then(Commands.argument("volume", StringArgumentType.greedyString())
                            .executes(SealCommand::sealOne));
                }))
                .executes(SealCommand::sealOne);
    }

    private static int sealAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player target = BreweryCommand.getPlayer(context);
        CommandSender sender = context.getSource().getSender();
        String volumeMessage = parseVolume(context);

        PlayerInventory targetInventory = target.getInventory();
        boolean oneFound = false;
        for (int i = 0; i < targetInventory.getSize(); i++) {
            ItemStack itemStack = targetInventory.getItem(i);
            if (itemStack == null) {
                continue;
            }
            Optional<Brew> brewOptional = BrewAdapter.fromItem(itemStack);
            if (brewOptional.isPresent()) {
                oneFound = true;
                targetInventory.setItem(i, BrewAdapter.toItem(brewOptional.get(), new BrewImpl.State.Seal(volumeMessage)));
            }
        }
        if (oneFound) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_SEAL_SUCCESS, Placeholder.unparsed("player_name", target.getName())));
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_SEAL_FAILURE));
        }
        return 1;
    }

    private static int sealOne(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player target = BreweryCommand.getPlayer(context);
        CommandSender sender = context.getSource().getSender();
        String volumeMessage = parseVolume(context);

        PlayerInventory targetInventory = target.getInventory();
        BrewAdapter.fromItem(targetInventory.getItemInMainHand())
                .map(brew -> BrewAdapter.toItem(brew, new BrewImpl.State.Seal(volumeMessage)))
                .ifPresentOrElse(itemStack -> {
                    targetInventory.setItemInMainHand(itemStack);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_SEAL_SUCCESS, Placeholder.unparsed("player_name", target.getName())));
                }, () -> {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_SEAL_FAILURE));
                });
        return 1;
    }

    private static @Nullable String parseVolume(CommandContext<CommandSourceStack> context) {
        try {
            String volumeMessage = context.getArgument("volume", String.class);
            if (!volumeMessage.isBlank()) {
                Component volumeMessageDeserialized = LegacyComponentSerializer.legacyAmpersand().deserialize(
                        volumeMessage
                );
                return MiniMessage.miniMessage().serialize(volumeMessageDeserialized);
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }
}
