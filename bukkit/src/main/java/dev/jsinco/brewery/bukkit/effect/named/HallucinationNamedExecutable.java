package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.bukkit.util.BlockUtil;
import dev.jsinco.brewery.bukkit.util.LocationUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class HallucinationNamedExecutable implements EventPropertyExecutable {

    private static final int BLOCK_RANGE = 20;
    private static final int TARGET_SEARCH_ATTEMPTS = 10;
    private static final List<Vector> ONE_BLOCK_OFFSETS = generateOffsets();

    private static @Nullable List<BlockType> REPLACEMENT_BLOCKS = null;
    private static final Set<Material> CANNOT_REPLACE = EnumSet.of(
            // physics desync
            Material.MAGMA_BLOCK,
            Material.SLIME_BLOCK,
            // texture is see-through
            Material.BARRIER
    );
    private static final Set<Material> PROBLEMATIC_REPLACEMENT_BLOCKS = EnumSet.of(
            // physics desync
            Material.MAGMA_BLOCK,
            Material.SLIME_BLOCK,
            // texture is see-through
            Material.BARRIER,
            Material.SPAWNER,
            Material.TRIAL_SPAWNER,
            Material.VAULT
    );

    private static List<Vector> generateOffsets() {
        List<Vector> offsets = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    offsets.add(new Vector(x, y, z));
                }
            }
        }
        return offsets;
    }

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        Block lookingAt = player.getTargetBlock(null, BLOCK_RANGE);
        Hallucination hallucination = attemptHallucination(lookingAt);
        if (hallucination == null) {
            return ExecutionResult.CONTINUE;
        }
        Block target = hallucination.target();
        BlockType replacement = hallucination.replacement();

        BlockData blockData = replacement.createBlockData();
        player.sendBlockChange(target.getLocation(), blockData);
        player.spawnParticle(Particle.DUST, target.getLocation().toCenterLocation(), 10, 0.5, 0.5, 0.5, new Particle.DustOptions(blockData.getMapColor(), 1f));
        return ExecutionResult.CONTINUE;
    }

    private static @Nullable Hallucination attemptHallucination(Block searchCenter) {
        // The block the player is looking at is preferred, try that first
        if (canReplace(searchCenter)) {
            BlockType replacement = getReplacementBlock(searchCenter);
            if (replacement != null) {
                return new Hallucination(searchCenter, replacement);
            }
        }

        // Look for possible locations in 3x3x3 cube, in random order
        Location searchCenterLocation = searchCenter.getLocation();
        List<Location> candidateLocations = ONE_BLOCK_OFFSETS.stream()
                .map(offset -> searchCenterLocation.clone().add(offset))
                .filter(LocationUtil::isWithinBuildLimit)
                .filter(Location::isChunkLoaded)
                .collect(Collectors.toCollection(ArrayList::new)); // list must be mutable
        Collections.shuffle(candidateLocations);

        for (int i = 0; i < TARGET_SEARCH_ATTEMPTS; i++) {
            Block possibleTarget = candidateLocations.get(i).getBlock();
            if (canReplace(possibleTarget)) {
                BlockType replacement = getReplacementBlock(possibleTarget);
                if (replacement != null) {
                    return new Hallucination(possibleTarget, replacement);
                }
            }
        }
        return null;
    }

    private record Hallucination(Block target, BlockType replacement) {}

    private static boolean canReplace(Block toReplace) {
        // Only replacing full blocks ensures no physics desyncs
        return !CANNOT_REPLACE.contains(toReplace.getType()) && toReplace.isSolid() && BlockUtil.isFullBlock(toReplace);
    }

    private static @Nullable BlockType getReplacementBlock(Block target) {
        BlockType targetType = Objects.requireNonNull(target.getType().asBlockType());
        List<BlockType> candidates = getReplacementBlocks(target.getWorld()).stream()
                .filter(replacement -> isSensibleReplacement(replacement, targetType))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(RANDOM.nextInt(candidates.size()));
    }

    private static boolean isSensibleReplacement(BlockType replacement, BlockType target) {
        return !replacement.equals(target) &&
                // So client and server agree on block friction
                replacement.getSlipperiness() == target.getSlipperiness() &&
                // So player can't mine block faster than usual
                replacementCannotBeMinedFaster(replacement, target);
    }

    private static boolean replacementCannotBeMinedFaster(BlockType replacement, BlockType target) {
        // Prevents player from using hallucinations to "mine" blocks faster than usual,
        // such as by replacing obsidian with dirt, (the obsidian will still take a long time before it's mined on
        // the server, but the client is not sending mine updates during that time, which can cause anticheat problems)
        return target.getHardness() <= replacement.getHardness() &&
                // Replacement block must not be mineable with a tool the original block is not mineable with
                BlockUtil.getFasterTools(target).containsAll(BlockUtil.getFasterTools(replacement));
    }

    private static List<BlockType> getReplacementBlocks(World world) {
        if (REPLACEMENT_BLOCKS == null) {
            REPLACEMENT_BLOCKS = computeReplacementBlocks(world);
        }
        return REPLACEMENT_BLOCKS;
    }

    private static List<BlockType> computeReplacementBlocks(World world) {
        return Arrays.stream(Material.values())
                .filter(material -> !PROBLEMATIC_REPLACEMENT_BLOCKS.contains(material))
                // Replacing with full, solid, opaque blocks ensures no physics desyncs,
                // and no transparent blocks replacing non-transparent blocks for accidental x-ray
                .filter(material -> isFullSolidOpaqueBlock(material, world))
                .map(Material::asBlockType)
                .toList();
    }

    private static boolean isFullSolidOpaqueBlock(Material material, World world) {
        if (material.isAir() || material.isLegacy()) {
            return false;
        }
        BlockType blockType = material.asBlockType();
        if (blockType == null) {
            return false;
        }
        if (!blockType.isSolid() || !blockType.isOccluding()) {
            return false;
        }
        Location sampleLocation = new Location(world, 0, 0, 0);
        return BlockUtil.isFullBlock(material.createBlockData().getCollisionShape(sampleLocation));
    }

    @Override
    public int priority() {
        return -1;
    }

}
