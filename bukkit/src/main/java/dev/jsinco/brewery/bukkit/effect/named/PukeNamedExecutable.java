package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.EventStepRegistry;
import dev.jsinco.brewery.event.ExecutableEventStep;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class PukeNamedExecutable implements ExecutableEventStep {

    public static final NamespacedKey PUKE_ITEM = new NamespacedKey("brewery", "puke");

    @Override
    public void execute(UUID contextPlayer, List<EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return;
        }

        PukeHandler pukeHandler = new PukeHandler(Config.config().puke().pukeTime(), player);
        TheBrewingProject.getInstance().getActiveEventsRegistry().registerActiveEvent(player.getUniqueId(), NamedDrunkEvent.PUKE, Config.config().puke().pukeTime());
        Bukkit.getScheduler().runTaskTimer(TheBrewingProject.getInstance(), pukeHandler::tick, 0, 1);
    }


    @Override
    public void register(EventStepRegistry registry) {
        registry.register(NamedDrunkEvent.PUKE, PukeNamedExecutable::new);
    }

    static class PukeHandler {
        private int countDown;
        private final Player player;

        PukeHandler(int pukingTicks, Player player) {
            this.countDown = pukingTicks;
            this.player = player;
        }


        public void tick(BukkitTask task) {
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
}
