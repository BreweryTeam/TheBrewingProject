package dev.jsinco.brewery.bukkit.integration.event.skript;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import dev.jsinco.brewery.api.util.BreweryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

public class TbpSkriptEventStructure extends Structure {


    private BreweryKey key;
    private Component displayName;
    private SectionNode source;
    private List<TriggerItem> code;

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (debug) {
            return String.format("TbpSkriptEventStructure(key='%s', displayName='%s')", key.minimalized(SkriptIntegration.NAMESPACE), MiniMessage.miniMessage().serialize(displayName));
        }
        return String.format("[tbp_]register event with key \"%s\" [and ][display ]name \"%s\"", key.minimalized(SkriptIntegration.NAMESPACE), MiniMessage.miniMessage().serialize(displayName));
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult, @UnknownNullability EntryContainer entryContainer) {
        this.key = BreweryKey.parse((String) args[0].getSingle());
        this.displayName = (Component) args[1].getSingle();
        if (entryContainer == null) {
            Skript.error("[TBP] Empty entry container, this event does nothing! ");
            return false;
        }
        this.source = entryContainer.getSource();
        return true;
    }

    @Override
    public boolean load() {
        this.code = ScriptLoader.loadItems(source);
        return true;
    }

    public void run() {
        // What to do here?
    }

    public static void register(SyntaxRegistry syntaxRegistry) {
        syntaxRegistry
                .register(SyntaxRegistry.Key.of("tbp_custom_event_structure"), DefaultSyntaxInfos.Structure.builder(TbpSkriptEventStructure.class)
                        .addPattern("[tbp_]register event with key %text% [and ][display ]name %text component%")
                        .supplier(TbpSkriptEventStructure::new)
                        .build()
                );
    }
}
