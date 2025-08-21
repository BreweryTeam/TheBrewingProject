package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.event.EventPropertyExecutable;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PukeNamedExecutable implements EventPropertyExecutable {

    public static final NamespacedKey PUKE_ITEM = new NamespacedKey("brewery", "puke");

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        PukeHandler pukeHandler = new PukeHandler(Config.config().puke().pukeTime(), player);
        TheBrewingProject.getInstance().getActiveEventsRegistry().registerActiveEvent(player.getUniqueId(), NamedDrunkEvent.fromKey("puke"), Config.config().puke().pukeTime());
        player.getScheduler().runAtFixedRate(TheBrewingProject.getInstance(), pukeHandler::tick, () -> {
        }, 1, 1);
        return ExecutionResult.CONTINUE;
    }

    static class PukeHandler {
        private int countDown;
        private final Player player;

        PukeHandler(int pukingTicks, Player player) {
            this.countDown = pukingTicks;
            this.player = player;
        }


        public void tick(ScheduledTask task) {
            if (!player.isOnline() || countDown-- <= 0) {
                task.cancel();
                return;
            }
            Location loc = player.getLocation();
            loc.setY(loc.getY() + 1.1);
            loc.setPitch(player.getPitch() + RANDOM.nextInt(-10, 11));
            loc.setYaw(player.getYaw() + RANDOM.nextInt(-10, 11));
            Vector direction = loc.getDirection();
            direction.multiply(0.5);
            loc.add(direction);
            Item item = player.getWorld().dropItem(loc, new ItemStack(Material.SOUL_SAND));
            item.setVelocity(direction);
            item.setPersistent(false);
            item.setCanPlayerPickup(false);
            item.setCanMobPickup(false);
            item.setPickupDelay(32767);
            item.getPersistentDataContainer().set(PUKE_ITEM, PersistentDataType.BOOLEAN, true);

            World world = loc.getWorld();

            YamlConfiguration spigotConfig = Bukkit.spigot().getConfig(); // Deprecated but no obvious replacement by paper yet?
            int worldDespawnRate = spigotConfig.getInt("world-settings." + world.getName() + ".item-despawn-rate", -1);
            if (worldDespawnRate < 0) {
                worldDespawnRate = spigotConfig.getInt("world-settings.default.item-despawn-rate", 6000);
            }
            int despawnRate = Math.max(Config.config().puke().pukeDespawnTime(), 4);
            item.setTicksLived(worldDespawnRate - despawnRate + RANDOM.nextInt(-despawnRate / 2, despawnRate / 2 + 1));
        }
    }

    @Override
    public int priority() {
        return -1;
    }
}
