package dev.jsinco.brewery.bukkit.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import dev.jsinco.brewery.bukkit.adapter.BukkitAdapter;
import dev.jsinco.brewery.util.ClassUtil;
import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;

public class BlockUtil {
    private static final boolean PROTOCOL_LIB_ENABLED = ClassUtil.exists("com.comphenix.protocol.events.PacketContainer");

    public static boolean isChunkLoaded(BreweryLocation block) {
        return BukkitAdapter.toLocation(block)
                .map(Location::isChunkLoaded)
                .orElse(false);
    }

    public static boolean isLitCampfire(Block block) {
        if (block.getType() == Material.CAMPFIRE || block.getType() == Material.SOUL_CAMPFIRE) {
            return ((Lightable) block.getBlockData()).isLit();
        }
        return false;
    }

    public static boolean isSource(Block block) {
        if (block.getType() == Material.LAVA || block.getType() == Material.WATER) {
            return ((Levelled) block.getBlockData()).getLevel() == 0;
        }
        return false;
    }

    public static void playWobbleEffect(BreweryLocation location, Player player) {
        if (!PROTOCOL_LIB_ENABLED) {
            return;
        }

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);
        packet.getBlockPositionModifier()
                .writeSafely(0, new BlockPosition(location.x(), location.y(), location.z()));
        packet.getBytes()
                .writeSafely(0, (byte) 1)
                .writeSafely(1, (byte) 1);
        packet.getIntegers().writeSafely(0, 1); // Block id (this field is not read anyhow
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }
}
