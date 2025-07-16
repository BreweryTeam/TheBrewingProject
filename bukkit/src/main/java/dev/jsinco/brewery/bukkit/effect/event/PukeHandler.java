package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.configuration.Config;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Random;

public class PukeHandler {
    private int countDown;
    private final Player player;
    private static final Random RANDOM = new Random();
    public static final NamespacedKey PUKE_ITEM = new NamespacedKey("brewery", "puke");

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

        File spigotYml = new File(Bukkit.getServer().getWorldContainer(), "spigot.yml");
        YamlConfiguration spigotConfig = YamlConfiguration.loadConfiguration(spigotYml);
        int worldDespawnRate = spigotConfig.getInt("world-settings." + world.getName() + ".item-despawn-rate", -1);
        if (worldDespawnRate < 0) {
            worldDespawnRate = spigotConfig.getInt("world-settings.default.item-despawn-rate", 6000);
        }
        int despawnRate = Math.max(Config.config().puke().pukeDespawnTime(), 4);
        item.setTicksLived(worldDespawnRate - despawnRate + RANDOM.nextInt(-despawnRate / 2, despawnRate / 2 + 1));
    }

}
