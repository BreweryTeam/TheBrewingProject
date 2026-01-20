package dev.jsinco.brewery.bukkit.command.argument;

import com.google.common.collect.Streams;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.integration.IntegrationType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.api.event.CustomEvent;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class EventArgument implements CustomArgumentType.Converted<DrunkEvent, String> {
    private static final DynamicCommandExceptionType ERROR_INVALID_EVENT = new DynamicCommandExceptionType(event ->
            BukkitMessageUtil.toBrigadier("tbp.command.illegal-argument-detailed", Placeholder.unparsed("arguments", event.toString()))
    );
    private static final Pattern EVENT_META_GREEDY_RE = Pattern.compile("([^{}]+)\\{(.*)}");

    @Override
    public DrunkEvent convert(String nativeType) throws CommandSyntaxException {
        BreweryKey key = BreweryKey.parse(nativeType);
        NamedDrunkEvent namedDrunkEvent = BreweryRegistry.DRUNK_EVENT.get(key);
        if (namedDrunkEvent != null) {
            return namedDrunkEvent;
        }
        CustomEvent.Keyed customEvent = TheBrewingProject.getInstance().getCustomDrunkEventRegistry().getCustomEvent(key);
        if (customEvent != null) {
            return customEvent;
        }
        Matcher matcher = EVENT_META_GREEDY_RE.matcher(nativeType);
        String meta;
        if (matcher.matches()) {
            String group2 = matcher.group(2);
            meta = group2.isBlank() ? null : group2;
            key = BreweryKey.parse(matcher.group(1));
        } else {
            key = BreweryKey.parse(nativeType);
            meta = null;
        }
        EventIntegration.SerializedEvent serializedEvent = new EventIntegration.SerializedEvent(key, meta);
        return TheBrewingProject.getInstance().getIntegrationManager()
                .getIntegrationRegistry()
                .getIntegrations(IntegrationTypes.EVENT)
                .stream()
                .map(eventIntegration -> eventIntegration.deserialize(serializedEvent))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> ERROR_INVALID_EVENT.create(nativeType));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        Stream.Builder<BreweryKey> keyBuilder = Stream.builder();
        Streams.concat(TheBrewingProject.getInstance().getCustomDrunkEventRegistry().events().stream(), BreweryRegistry.DRUNK_EVENT.values().stream())
                .map(DrunkEvent::key)
                .forEach(keyBuilder);
        TheBrewingProject.getInstance().getIntegrationManager().getIntegrationRegistry()
                .getIntegrations(IntegrationTypes.EVENT)
                .stream()
                .map(EventIntegration::listEventKeys)
                .flatMap(Collection::stream)
                .forEach(keyBuilder);
        String remaining = ArgumentUtil.escapeQuotes(builder.getRemainingLowerCase());
        keyBuilder.build()
                .map(BreweryKey::minimalized)
                .filter(event -> event.startsWith(remaining))
                .map(ArgumentUtil::sanitizeName)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
