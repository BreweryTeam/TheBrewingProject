package dev.jsinco.brewery.paper.listeners;

import dev.jsinco.brewery.paper.TheBrewingProject;
import dev.jsinco.brewery.api.math.BlockPos;
import dev.jsinco.brewery.paper.object.cauldron.BukkitCauldron;
import dev.jsinco.brewery.paper.recipe.ingredient.IngredientManager;
import dev.jsinco.brewery.paper.util.Util;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ListIterator;

public class BreweryEvents implements Listener {

    private void handleBlockDestroyEvent(BlockEvent event) {
        TheBrewingProject.getInstance().getCauldronManager().removeCauldron(
                new BlockPos(event.getBlock().getLocation())
        );
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleBlockDestroyEvent(event);
    }

    @EventHandler
    public void onBlockExplode(EntityExplodeEvent event) {
        ListIterator<Block> iter = event.blockList().listIterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            // TODO: CauldronManager.removeCauldronsThatAreAffectedByThisExplosion();
        }
    }

    private void handleCauldronInteract(Block block, ItemStack item, PlayerInteractEvent event) {
        Levelled levelled = (Levelled) block.getBlockData();
        if (levelled.getLevel() == 0) return;

        BukkitCauldron cauldron = (BukkitCauldron) TheBrewingProject.getInstance().getCauldronManager().ensureAndGetCauldron(BlockPos.fromLocation(block.getLocation()));

        cauldron.addIngredient(IngredientManager.getIngredient(item));
        item.setAmount(item.getAmount() - 1);
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block != null) {
            if (Util.isCauldron(block.getType()) && item != null) {
                handleCauldronInteract(block, item, event);
            }
        }
    }
}
