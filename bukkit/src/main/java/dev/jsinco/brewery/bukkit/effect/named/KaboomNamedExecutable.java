package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.ExecutableEventStep;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class KaboomNamedExecutable implements ExecutableEventStep {

    private static final Vector UPWARDS = new Vector(0, 1, 0);

    @Override
    public void execute(UUID contextPlayer, List<EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return;
        }

        World w = player.getWorld();
        w.spawnParticle(Particle.EXPLOSION, player.getLocation(), 1, 0, 0, 0, 0.1);
        w.spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 3, 0.5, 0.5, 0.5, 0.1);
        w.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        player.setVelocity(UPWARDS.multiply(1.5));

        double targetHealth = 8.0;
        double currentHealth = player.getHealth();
        double damageAmount = currentHealth - targetHealth;

        if (damageAmount > 0) {
            player.damage(damageAmount);
        }
    }

}
