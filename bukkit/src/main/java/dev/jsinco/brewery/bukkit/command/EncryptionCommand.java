package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;

public class EncryptionCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("rotate_key")
                .executes(context -> {
                    rotateKey(context.getSource().getSender(), null);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("reencrypt", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean reencrypt = BoolArgumentType.getBool(context, "reencrypt");
                            rotateKey(context.getSource().getSender(), reencrypt);
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                );
    }

    private static void rotateKey(CommandSender sender, Boolean reencrypt) {
        if (reencrypt != null) Config.config().set("reencryptItemsInInventories", reencrypt);
        List<SecretKey> previousKeys = Config.config().previousEncryptionKeys();
        previousKeys.add(Config.config().encryptionKey());
        Config.config().set("previousEncryptionKeys", previousKeys.stream().map(key -> Base64.getEncoder().encodeToString(key.getEncoded())).toList());
        Config.config().set("encryptionKey", Base64.getEncoder().encodeToString(Config.generateAesKey().getEncoded()));
        Config.config().save();
        MessageUtil.message(sender, "tbp.command.encryption.rotate_key");
    }

}
