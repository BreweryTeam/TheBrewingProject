package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.util.SoundPlayer;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.event.EventPropertyExecutable;
import dev.jsinco.brewery.event.EventStep;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class KaboomNamedExecutable implements EventPropertyExecutable {

    private static final Random random = new Random();

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }
        World world = player.getWorld();
        if (RANDOM.nextInt(1000000) == 0) {
            // Trolllolololololololololol
            world.spawnParticle(Particle.EXPLOSION, player.getLocation(), 1, 0, 0, 0, 0.1);
            world.spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 3, 0.5, 0.5, 0.5, 0.1);
            SoundPlayer.playSoundEffect(Config.config().sounds().kaboom(), Sound.Source.PLAYER, player.getLocation());
            player.setVelocity(new Vector(0, 100, 0));
        } else {
            world.spawnParticle(Particle.ENTITY_EFFECT, player.getLocation(), 10, 0, 0.5, 0, Color.GREEN);
            SoundPlayer.playSoundEffect(Config.config().sounds().kaboom(), Sound.Source.PLAYER, player.getLocation());
            player.setVelocity(new Vector(0, EventSection.events().kaboomVelocity(), 0));
        }


        double targetHealth = 8.0;
        double currentHealth = player.getHealth();
        double damageAmount = currentHealth - targetHealth;

        if (damageAmount > 0) {
            player.damage(damageAmount);
        }
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
