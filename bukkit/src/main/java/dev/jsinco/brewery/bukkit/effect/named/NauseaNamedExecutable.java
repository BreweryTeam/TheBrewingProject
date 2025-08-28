package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.moment.Moment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class NauseaNamedExecutable implements EventPropertyExecutable {

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, RANDOM.nextInt(Moment.MINUTE / 2, Moment.MINUTE * 3 / 2), 1));
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
