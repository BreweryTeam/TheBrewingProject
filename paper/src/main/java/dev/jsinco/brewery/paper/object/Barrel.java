package dev.jsinco.brewery.paper.object;

import dev.jsinco.brewery.api.math.BoundingBox;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Class for traditional barrels which use BoundingBoxes to determine the area of the barrel.
 */
@Getter
public class Barrel implements InventoryHolder {

    private final UUID objectId;
    private final BoundingBox boundingBox;
    private final Location barrelSign;
    private final Inventory inventory;

    public Barrel(BoundingBox boundingBox, Location barrelSign) {
        this.objectId = UUID.randomUUID();
        this.boundingBox = boundingBox;
        this.barrelSign = barrelSign;
        this.inventory = Bukkit.createInventory(this, 27, "Barrel");
    }

    public Barrel(UUID objectId, BoundingBox boundingBox, Location barrelSign, Inventory inventory) {
        this.objectId = objectId;
        this.boundingBox = boundingBox;
        this.barrelSign = barrelSign;
        this.inventory = inventory;
    }


    public void open(Player player) {
        float randPitch = (float) (Math.random() * 0.1);
        barrelSign.getWorld().playSound(barrelSign, Sound.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 0.5f, 0.8f + randPitch);
        player.openInventory(inventory);
    }

    public void close(Player player) {
        float randPitch = (float) (Math.random() * 0.1);
        barrelSign.getWorld().playSound(barrelSign, Sound.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS, 0.5f, 0.8f + randPitch);
        player.closeInventory();
    }

    // Should we really tick barrels?
//    @Override
//    public void tick() {
//    }
}
