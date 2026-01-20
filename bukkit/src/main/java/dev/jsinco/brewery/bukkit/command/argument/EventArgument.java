package dev.jsinco.brewery.bukkit.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.api.event.CustomEvent;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.bukkit.util.EventUtil;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EventArgument implements CustomArgumentType.Converted<DrunkEvent, String> {
    private static final DynamicCommandExceptionType ERROR_INVALID_EVENT = new DynamicCommandExceptionType(event ->
            BukkitMessageUtil.toBrigadier("tbp.command.illegal-argument-detailed", Placeholder.unparsed("arguments", event.toString()))
    );

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
        EventIntegration.SerializedEvent serializedEvent = EventIntegration.parseEvent(nativeType);
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
    public <S> @NonNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        List<BreweryKey> all = EventUtil.listAll();
        String remaining = ArgumentUtil.escapeQuotes(builder.getRemainingLowerCase());
        all.stream()
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
