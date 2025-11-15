package dev.jsinco.brewery.bukkit.task;

import dev.jsinco.brewery.api.vector.BreweryLocation;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import dev.jsinco.brewery.configuration.CauldronSection;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.format.TimeFormat;
import dev.jsinco.brewery.format.TimeFormatter;
import dev.jsinco.brewery.format.TimeModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Task that displays cauldron brewing time in the action bar when a player
 * is looking at a cauldron while holding a clock in their main or off-hand.
 * Updates every second while looking at the cauldron to show current time.
 */
public class CauldronHoverTask extends BukkitRunnable {
    
    private final BreweryRegistry breweryRegistry;
    private final Map<UUID, BreweryLocation> lastCauldronLookup = new HashMap<>();
    
    public CauldronHoverTask(BreweryRegistry breweryRegistry) {
        this.breweryRegistry = breweryRegistry;
    }
    
    @Override
    public void run() {
        // Only run if hover mode is enabled
        if (Config.config().cauldrons().clockTimeActionBar() != CauldronSection.ClockActionBarMode.HOVER) {
            return;
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Check if player has permission
            if (!player.hasPermission("brewery.cauldron.time")) {
                continue;
            }
            
            // Check if player is holding a clock in main or off-hand
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            
            if (mainHand.getType() != Material.CLOCK && offHand.getType() != Material.CLOCK) {
                // Clear last lookup and action bar if not holding clock anymore
                if (lastCauldronLookup.remove(player.getUniqueId()) != null) {
                    player.sendActionBar(Component.empty());
                }
                continue;
            }
            
            // Get the block the player is looking at (max 5 blocks away)
            Block targetBlock = player.getTargetBlockExact(5);
            
            if (targetBlock == null || !isCauldronBlock(targetBlock.getType())) {
                // Clear last lookup and action bar if not looking at cauldron
                if (lastCauldronLookup.remove(player.getUniqueId()) != null) {
                    player.sendActionBar(Component.empty());
                }
                continue;
            }
            
            // Check if this is an active brewing cauldron
            BreweryLocation location = BukkitAdapter.toBreweryLocation(targetBlock);
            BukkitCauldron cauldron = breweryRegistry.getActiveSinglePositionStructure(location)
                    .filter(BukkitCauldron.class::isInstance)
                    .map(BukkitCauldron.class::cast)
                    .orElse(null);
            
            if (cauldron == null) {
                // Clear last lookup and action bar if not a brewing cauldron
                if (lastCauldronLookup.remove(player.getUniqueId()) != null) {
                    player.sendActionBar(Component.empty());
                }
                continue;
            }
            
            // Send updated action bar message every tick (will update every second)
            Component timeMessage = Component.translatable("tbp.cauldron.clock-message", Argument.tagResolver(
                    Placeholder.parsed("time", TimeFormatter.format(cauldron.getTime(), TimeFormat.CLOCK_MECHANIC, TimeModifier.COOKING))
            ));
            player.sendActionBar(timeMessage);
            
            // Remember this cauldron
            lastCauldronLookup.put(player.getUniqueId(), location);
        }
    }
    
    /**
     * Checks if the given material is a cauldron type
     */
    private boolean isCauldronBlock(Material material) {
        return material == Material.CAULDRON || 
               material == Material.WATER_CAULDRON || 
               material == Material.LAVA_CAULDRON || 
               material == Material.POWDER_SNOW_CAULDRON;
    }
    
    /**
     * Starts the hover detection task
     */
    public void start() {
        // Run every 20 ticks (1 second) to update the action bar
        this.runTaskTimer(TheBrewingProject.getInstance(), 0L, 20L);
    }
}
