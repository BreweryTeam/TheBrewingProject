package dev.jsinco.brewery.bukkit.integration.event.skript;

import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Collection;
import java.util.List;

public class TbpSyntaxRegistry implements SyntaxRegistry {
    @Override
    public @Unmodifiable <I extends SyntaxInfo<?>> Collection<I> syntaxes(Key<I> key) {
        return List.of();
    }

    @Override
    public <I extends SyntaxInfo<?>> void register(Key<I> key, I info) {

    }

    @Override
    public void unregister(SyntaxInfo<?> info) {

    }

    @Override
    public <I extends SyntaxInfo<?>> void unregister(Key<I> key, I info) {

    }

    @Override
    public @Unmodifiable Collection<SyntaxInfo<?>> elements() {
        return List.of();
    }
}
