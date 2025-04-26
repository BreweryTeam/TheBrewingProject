package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.breweries.Distillery;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.brew.BukkitDistilleryBrewDataType;
import dev.jsinco.brewery.bukkit.structure.PlacedBreweryStructure;
import dev.jsinco.brewery.bukkit.util.BlockUtil;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.structure.StructureMeta;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.vector.BreweryLocation;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BukkitDistillery implements Distillery<BukkitDistillery, ItemStack, Inventory> {

    @Getter
    private final PlacedBreweryStructure<BukkitDistillery> structure;
    @Getter
    private long startTime;
    @Getter
    private final DistilleryInventory mixture;
    @Getter
    private final DistilleryInventory distillate;
    private boolean dirty = true;
    private final Set<BreweryLocation> mixtureContainerLocations = new HashSet<>();
    private final Set<BreweryLocation> distillateContainerLocations = new HashSet<>();


    public BukkitDistillery(@NotNull PlacedBreweryStructure<BukkitDistillery> structure) {
        this(structure, TheBrewingProject.getInstance().getTime());
    }

    public BukkitDistillery(@NotNull PlacedBreweryStructure<BukkitDistillery> structure, long startTime) {
        this.structure = structure;
        this.startTime = startTime;
        this.mixture = new DistilleryInventory("Distillery Mixture", structure.getStructure().getMeta(StructureMeta.INVENTORY_SIZE), this);
        this.distillate = new DistilleryInventory("Distillery Distillate", structure.getStructure().getMeta(StructureMeta.INVENTORY_SIZE), this);
    }

    public boolean open(@NotNull BreweryLocation location, @NotNull UUID playerUuid) {
        checkDirty();
        Player player = Bukkit.getPlayer(playerUuid);
        if (mixtureContainerLocations.contains(location)) {
            return openInventory(mixture, player);
        }
        if (distillateContainerLocations.contains(location)) {
            return openInventory(distillate, player);
        }
        return false;
    }

    private boolean openInventory(DistilleryInventory inventory, Player player) {
        if (!player.hasPermission("brewery.distillery.access")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.DISTILLERY_ACCESS_DENIED));
            return false;
        }
        inventory.updateInventoryFromBrews();
        TheBrewingProject.getInstance().getBreweryRegistry().registerOpened(this);
        player.openInventory(inventory.getInventory());
        return true;
    }

    @Override
    public boolean inventoryAllows(@NotNull UUID playerUuid, @NotNull ItemStack item) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) {
            return false;
        }
        if (!player.hasPermission("brewery.distillery.access")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.DISTILLERY_ACCESS_DENIED));
            return false;
        }
        return BrewAdapter.fromItem(item).isPresent();
    }

    @Override
    public Set<Inventory> getInventories() {
        return Set.of(mixture.getInventory(), distillate.getInventory());
    }

    /**
     * Made to avoid chunk access on startup
     */
    private void checkDirty() {
        if (!dirty) {
            return;
        }
        dirty = false;
        Set<BreweryLocation> potLocations = new HashSet<>();
        Material taggedMaterial = Material.valueOf(structure.getStructure().getMeta(StructureMeta.TAGGED_MATERIAL).toUpperCase(Locale.ROOT));
        for (BreweryLocation location : structure.positions()) {
            Block block = BukkitAdapter.toBlock(location);
            if (taggedMaterial == block.getType()) {
                potLocations.add(location);
            }
        }
        for (BreweryLocation breweryLocation : potLocations) {
            if (potLocations.contains(breweryLocation.add(0, 1, 0)) || potLocations.contains(breweryLocation.add(0, -1, 0))) {
                mixtureContainerLocations.add(breweryLocation);
            } else {
                distillateContainerLocations.add(breweryLocation);
            }
        }
    }

    public void tick() {
        BreweryLocation unique = getStructure().getUnique();
        long timeProcessed = getTimeProcessed();
        long processTime = getProcessTime();
        if (!BlockUtil.isChunkLoaded(unique)
                || mixture.brewAmount() < (timeProcessed / processTime) * getStructure().getStructure().getMeta(StructureMeta.PROCESS_AMOUNT)
                || distillate.isFull()) {
            return;
        }
        if (timeProcessed % processTime == 0 && timeProcessed != 0) {
            BukkitAdapter.toWorld(unique)
                    .ifPresent(world -> world.playSound(Sound.sound()
                                    .source(Sound.Source.BLOCK)
                                    .type(Key.key("block.brewing_stand.brew"))
                                    .build(),
                            unique.x(), unique.y(), unique.z()
                    ));
        }
        if (timeProcessed % (processTime / 4) < processTime / 16) {
            distillateContainerLocations.stream()
                    .map(BukkitAdapter::toLocation)
                    .map(location -> location.add(0.5, 1.3, 0.5))
                    .forEach(location -> location.getWorld().spawnParticle(Particle.ENTITY_EFFECT, location, 2, Color.WHITE));
        }
    }

    public void tickInventory() {
        checkDirty();
        boolean hasChanged;
        if (mixture.getInventory().getViewers().isEmpty()) {
            mixture.getInventory().clear(); // Depopulate inventory, save on memory
            hasChanged = false;
        } else {
            hasChanged = mixture.updateBrewsFromInventory();
        }
        if (distillate.getInventory().getViewers().isEmpty()) {
            distillate.getInventory().clear(); // Depopulate inventory, save on memory
        } else {
            distillate.updateBrewsFromInventory();
        }
        if (hasChanged) {
            resetStartTime();
        }
        if (distillate.getInventory().getViewers().isEmpty() && mixture.getInventory().getViewers().isEmpty()) {
            TheBrewingProject.getInstance().getBreweryRegistry().unregisterOpened(this);
        }
        long diff = getTimeProcessed();
        long processTime = getProcessTime();
        if (diff < processTime || mixture.isEmpty()) {
            return;
        }
        transferItems(mixture, distillate, (int) (getStructure().getStructure().getMeta(StructureMeta.PROCESS_AMOUNT) * (diff / processTime)));
        if (!distillate.getInventory().getViewers().isEmpty()) {
            distillate.updateInventoryFromBrews();
        }
        if (!mixture.getInventory().getViewers().isEmpty()) {
            mixture.updateInventoryFromBrews();
        }
        resetStartTime();
    }

    private long getTimeProcessed() {
        return TheBrewingProject.getInstance().getTime() - startTime;
    }

    private void resetStartTime() {
        startTime = TheBrewingProject.getInstance().getTime();
    }

    private long getProcessTime() {
        return getStructure().getStructure().getMeta(StructureMeta.PROCESS_TIME);
    }

    private void transferItems(DistilleryInventory inventory1, DistilleryInventory inventory2, int amount) {
        Queue<Pair<Brew, Integer>> brewsToTransfer = new LinkedList<>();
        for (int i = 0; i < inventory1.brews.length; i++) {
            if (inventory1.getBrews()[i] == null) {
                continue;
            }
            if (amount-- <= 0) {
                break;
            }
            brewsToTransfer.add(new Pair<>(inventory1.brews[i], i));
        }
        for (int i = 0; i < inventory2.brews.length; i++) {
            if (inventory2.getBrews()[i] != null) {
                continue;
            }
            if (brewsToTransfer.isEmpty()) {
                return;
            }
            Pair<Brew, Integer> nextBrewToTransfer = brewsToTransfer.poll();
            Brew mixtureBrew = nextBrewToTransfer.first();
            inventory1.store(null, nextBrewToTransfer.second());
            inventory2.store(mixtureBrew.withLastStep(
                            BrewingStep.Distill.class,
                            BrewingStep.Distill::incrementAmount,
                            () -> new BrewingStep.Distill(1))
                    , i);
        }
    }

    @Override
    public void destroy(BreweryLocation breweryLocation) {
        Location location = BukkitAdapter.toLocation(breweryLocation).add(0.5, 0, 0.5);
        for (DistilleryInventory distilleryInventory : List.of(distillate, mixture)) {
            List.copyOf(distilleryInventory.getInventory().getViewers()).forEach(HumanEntity::closeInventory);
            distilleryInventory.getInventory().clear();
            for (Brew brew : distilleryInventory.getBrews()) {
                if (brew == null) {
                    continue;
                }
                location.getWorld().dropItem(location, BrewAdapter.toItem(brew, new Brew.State.Other()));
            }
        }
    }

    public static class DistilleryInventory implements InventoryHolder {

        private final Inventory inventory;
        @Getter
        private final Brew[] brews;
        private final BukkitDistillery owner;

        public DistilleryInventory(String title, int size, BukkitDistillery owner) {
            this.inventory = Bukkit.createInventory(this, size, title);
            this.brews = new Brew[size];
            this.owner = owner;
        }

        @NotNull
        @Override
        public Inventory getInventory() {
            return inventory;
        }

        /**
         * Set an item in the inventory without changing the database
         *
         * @param brew
         * @param position
         */
        public void set(@Nullable Brew brew, int position) {
            brews[position] = brew;
        }

        /**
         * Set an item in the inventory and store the changes in the database
         *
         * @param brew
         * @param position
         */
        public void store(@Nullable Brew brew, int position) {
            if (Objects.equals(brews[position], brew)) {
                return;
            }
            try {
                Brew previous = brews[position];
                set(brew, position);
                BreweryLocation unique = owner.getStructure().getUnique();
                BukkitDistilleryBrewDataType.DistilleryContext context = new BukkitDistilleryBrewDataType.DistilleryContext(unique.x(), unique.y(), unique.z(), unique.worldUuid(), position, this == owner.getDistillate());
                Pair<Brew, BukkitDistilleryBrewDataType.DistilleryContext> data = new Pair<>(brew, context);
                if (previous == null) {
                    TheBrewingProject.getInstance().getDatabase().insertValue(BukkitDistilleryBrewDataType.INSTANCE, data);
                    return;
                }
                if (brew == null) {
                    TheBrewingProject.getInstance().getDatabase().remove(BukkitDistilleryBrewDataType.INSTANCE, data);
                    return;
                }
                TheBrewingProject.getInstance().getDatabase().updateValue(BukkitDistilleryBrewDataType.INSTANCE, data);
            } catch (PersistenceException e) {
                e.printStackTrace();
            }

        }

        public void updateInventoryFromBrews() {
            for (int i = 0; i < brews.length; i++) {
                Brew brew = brews[i];
                if (brew == null) {
                    inventory.setItem(i, null);
                    continue;
                }
                inventory.setItem(i, BrewAdapter.toItem(brew, new Brew.State.Brewing()));
            }
        }

        public boolean updateBrewsFromInventory() {
            boolean hasUpdated = false;
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                Brew brew;
                if (itemStack == null) {
                    brew = null;
                } else {
                    brew = BrewAdapter.fromItem(itemStack).orElse(null);
                }
                if (!Objects.equals(brew, brews[i])) {
                    hasUpdated = true;
                }
                store(brew, i);
            }
            return hasUpdated;
        }

        public boolean isEmpty() {
            for (Brew brew : brews) {
                if (brew != null) {
                    return false;
                }
            }
            return true;
        }

        public boolean isFull() {
            for (Brew brew : brews) {
                if (brew == null) {
                    return false;
                }
            }
            return true;
        }

        public int brewAmount() {
            int amount = 0;
            for (Brew brew : brews) {
                if (brew != null) {
                    amount++;
                }
            }
            return amount;
        }
    }
}
