package dev.jsinco.brewery.bukkit.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class RegistryArgument<T extends BreweryKeyed> implements CustomArgumentType.Converted<T, String> {

    private final BreweryRegistry<T> registry;
    private static final DynamicCommandExceptionType ERROR_INVALID_ARGUMENT = new DynamicCommandExceptionType(event ->
            BukkitMessageUtil.toBrigadier("tbp.command.illegal-argument-detailed", Placeholder.unparsed("arguments", event.toString()))
    );

    public RegistryArgument(BreweryRegistry<T> registry) {
        this.registry = registry;
    }

    @Override
    public T convert(String nativeType) throws CommandSyntaxException {
        T t = registry.get(BreweryKey.parse(nativeType));
        if (t == null) {
            throw ERROR_INVALID_ARGUMENT.create(nativeType);
        }
        return t;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = ArgumentUtil.escapeQuotes(builder.getRemainingLowerCase());
        registry.values().stream()
                .map(BreweryKeyed::key)
                .map(BreweryKey::minimalized)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .filter(name -> name.contains(remaining))
                .map(ArgumentUtil::sanitizeName)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
