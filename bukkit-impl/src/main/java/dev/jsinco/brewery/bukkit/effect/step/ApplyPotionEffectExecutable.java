package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.ExecutionOutcome;
import dev.jsinco.brewery.api.event.step.ApplyPotionEffect;
import dev.jsinco.brewery.api.moment.Interval;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.recipe.RecipeEffectImpl;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;


public record ApplyPotionEffectExecutable(String potionEffectName, Interval amplifierBounds,
                                          Interval durationBounds) implements EventPropertyExecutable {


    @Override
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> eventStepProperties) {
        executeFor(contextPlayer);
        return ExecutionResult.CONTINUE;
    }

    @Override
    public ExecutionOutcome executeFor(UUID contextPlayer) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return new ExecutionOutcome.Continue();
        }
        NamespacedKey key = NamespacedKey.fromString(potionEffectName);
        if (key == null) {
            return new ExecutionOutcome.Continue();
        }
        PotionEffect potionEffect = new RecipeEffectImpl(
                Registry.EFFECT.get(key),
                durationBounds,
                amplifierBounds
        ).newPotionEffect();
        player.getScheduler().run(TheBrewingProject.getInstance(), ignored -> player.addPotionEffect(potionEffect), null);
        return new ExecutionOutcome.Continue();
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public EventStepProperty toProperty() {
        return new ApplyPotionEffect(potionEffectName, amplifierBounds, durationBounds);
    }

    @Override
    public ExecutionContext context() {
        return ExecutionContext.PLAYER;
    }

    @Override
    public EventPropertyExecutable withSkipPoint(@Nullable EventPropertyExecutable point) {
        return this; // NO-OP
    }
}
