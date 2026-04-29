package dev.jsinco.brewery.bukkit.integration.event.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import dev.jsinco.brewery.api.util.BreweryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

public class TbpSkriptEventStructure extends Structure {


    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "";
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult, @UnknownNullability EntryContainer entryContainer) {
        BreweryKey key = null;
        Component displayName = null;
        for (Literal<?> argument : args) {
            Literal<? extends String> converted = argument.getConvertedExpression(String.class);
            if (converted == null) {
                Skript.error("[TBP] Could not parse argument of event; could not convert value to string.");
                return false;
            }
        }
    }

    @Override
    public boolean load() {
        return false;
    }
}
