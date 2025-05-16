package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.configuration.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Random;

class PukeHandler {
    private int countDown;
    private final Player player;
    private static final Random RANDOM = new Random();

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
        item.setPickupDelay(32767);

        World world = loc.getWorld();
        YamlConfiguration spigotConfig = Bukkit.spigot().getConfig();
        int worldDespawnRate = spigotConfig.getInt("world-settings." + world.getName() + ".item-despawn-rate", -1);
        if (worldDespawnRate < 0) {
            worldDespawnRate = spigotConfig.getInt("world-settings.default.item-despawn-rate", 6000);
        }
        int despawnRate = Math.max(Config.PUKE_DESPAWN_RATE, 4);
        item.setTicksLived(worldDespawnRate - despawnRate + RANDOM.nextInt(-despawnRate / 2, despawnRate / 2 + 1));
    }
}
