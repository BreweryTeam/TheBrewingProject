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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class EnumArgument<E extends Enum<E>> implements CustomArgumentType.Converted<E, String> {
    private static final DynamicCommandExceptionType ERROR_INVALID_ENUM = new DynamicCommandExceptionType(event ->
            MessageComponentSerializer.message().serialize(MessageUtil.mm(TranslationsConfig.COMMAND_ILLEGAL_ARGUMENT_DETAILED, Placeholder.unparsed("argument", event.toString())))
    );
    private final Class<E> eClass;

    public EnumArgument(Class<E> eClass) {
        this.eClass = eClass;
    }

    @Override
    public E convert(String nativeType) throws CommandSyntaxException {
        try {
            return E.valueOf(eClass, nativeType.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ERROR_INVALID_ENUM.create(nativeType);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        Arrays.stream(eClass.getEnumConstants())
                .map(E::name)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .filter(enumConstant -> enumConstant.startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
