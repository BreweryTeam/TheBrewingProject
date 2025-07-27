package dev.jsinco.brewery.bukkit.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class OnlinePlayerArgument implements CustomArgumentType.Converted<Player, String> {

    private static final DynamicCommandExceptionType ERROR_INVALID_PLAYER = new DynamicCommandExceptionType(invalidPlayer ->
            MessageComponentSerializer.message().serialize(MessageUtil.miniMessage(TranslationsConfig.COMMAND_UNKNOWN_PLAYER, Placeholder.unparsed("player_name", invalidPlayer.toString())))
    );

    @Override
    public Player convert(String nativeType) throws CommandSyntaxException {
        Player player = Bukkit.getPlayer(nativeType);
        if (player == null) {
            throw ERROR_INVALID_PLAYER.create(nativeType);
        }
        return player;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        Bukkit.getOnlinePlayers().stream()
                .map(OfflinePlayer::getName)
                .filter(event -> event.startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
