package dev.jsinco.brewery.bukkit.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OfflinePlayerArgument implements CustomArgumentType<OfflinePlayerSelectorArgumentResolver, PlayerSelectorArgumentResolver> {

    public static final OfflinePlayerArgument SINGLE = new OfflinePlayerArgument(ArgumentTypes.player());
    public static final OfflinePlayerArgument MULTIPLE = new OfflinePlayerArgument(ArgumentTypes.players());

    private static final DynamicCommandExceptionType ERROR_INVALID_PLAYER = new DynamicCommandExceptionType(invalidPlayer ->
            BukkitMessageUtil.toBrigadier("tbp.command.unknown-player", Placeholder.unparsed("player_name", invalidPlayer.toString()))
    );
    private final ArgumentType<PlayerSelectorArgumentResolver> backing;

    private OfflinePlayerArgument(ArgumentType<PlayerSelectorArgumentResolver> backing) {
        this.backing = backing;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        return backing.listSuggestions(context, builder);
    }

    @Override
    public OfflinePlayerSelectorArgumentResolver parse(StringReader reader) throws CommandSyntaxException {
        char startChar = reader.peek();
        if (startChar == '@') {
            PlayerSelectorArgumentResolver playerSelectorArgumentResolver = backing.parse(reader);
            return commandSourceStack -> playerSelectorArgumentResolver
                    .resolve(commandSourceStack)
                    .stream()
                    .map(player -> (OfflinePlayer) player)
                    .toList();
        }
        String playerName = reader.readString();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(playerName);
        if(offlinePlayer == null) {
            throw ERROR_INVALID_PLAYER.create(playerName);
        }
        return ignored -> List.of(offlinePlayer);
    }

    @Override
    public @NotNull ArgumentType<PlayerSelectorArgumentResolver> getNativeType() {
        return backing;
    }
}
