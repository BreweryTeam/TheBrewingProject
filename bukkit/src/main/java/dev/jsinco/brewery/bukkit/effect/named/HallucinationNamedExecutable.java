package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.event.EventPropertyExecutable;
import dev.jsinco.brewery.event.EventStep;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class HallucinationNamedExecutable implements EventPropertyExecutable {

    private static final int BLOCK_RANGE = 20;

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        Block block = player.getTargetBlock(null, BLOCK_RANGE);
        // Random material, keep looping until we get a material that can be a block
        Material material;
        do {
            material = Material.values()[RANDOM.nextInt(Material.values().length)];
        } while (!material.isBlock() || material.isAir() || material.isLegacy());

        BlockData blockData = material.createBlockData();

        player.sendBlockChange(block.getLocation(), blockData);
        player.spawnParticle(Particle.DUST, block.getLocation().toCenterLocation(), 10, 0.5, 0.5, 0.5, new Particle.DustOptions(blockData.getMapColor(), 1f));
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
