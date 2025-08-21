package dev.jsinco.brewery.bukkit.command.argument;

import com.google.common.collect.Streams;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.event.CustomEvent;
import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Registry;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class EventArgument implements CustomArgumentType.Converted<DrunkEvent, String> {
    private static final DynamicCommandExceptionType ERROR_INVALID_EVENT = new DynamicCommandExceptionType(event ->
            BukkitMessageUtil.toBrigadier("tbp.command.illegal-argument-detailed", Placeholder.unparsed("argument", event.toString()))
    );

    @Override
    public DrunkEvent convert(String nativeType) throws CommandSyntaxException {
        BreweryKey key = BreweryKey.parse(nativeType);
        NamedDrunkEvent namedDrunkEvent = Registry.DRUNK_EVENT.get(key);
        if (namedDrunkEvent != null) {
            return namedDrunkEvent;
        }
        CustomEvent.Keyed customEvent = TheBrewingProject.getInstance().getCustomDrunkEventRegistry().getCustomEvent(key);
        if (customEvent != null) {
            return customEvent;
        }
        throw ERROR_INVALID_EVENT.create(nativeType);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        Stream<CustomEvent.Keyed> customEventStream = TheBrewingProject.getInstance().getCustomDrunkEventRegistry().events().stream();
        Streams.concat(Registry.DRUNK_EVENT.values().stream(), customEventStream)
                .map(DrunkEvent::key)
                .map(BreweryKey::key)
                .filter(event -> event.startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
