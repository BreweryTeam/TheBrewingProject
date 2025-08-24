package dev.jsinco.brewery.bukkit.breweries.distillery;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.breweries.Distillery;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.breweries.BrewInventory;
import dev.jsinco.brewery.bukkit.structure.PlacedBreweryStructure;
import dev.jsinco.brewery.bukkit.util.BlockUtil;
import dev.jsinco.brewery.bukkit.adapter.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.SoundPlayer;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.structure.StructureMeta;
import dev.jsinco.brewery.util.Logger;
import dev.jsinco.brewery.util.MessageUtil;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.vector.BreweryLocation;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class BukkitDistillery implements Distillery<BukkitDistillery, ItemStack, Inventory> {

    @Getter
    private final PlacedBreweryStructure<BukkitDistillery> structure;
    @Getter
    private long startTime;
    @Getter
    private final BrewInventory mixture;
    @Getter
    private final BrewInventory distillate;
    private boolean dirty = true;
    private final Set<BreweryLocation> mixtureContainerLocations = new HashSet<>();
    private final Set<BreweryLocation> distillateContainerLocations = new HashSet<>();
    private long recentlyAccessed = -1L;


    public BukkitDistillery(@NotNull PlacedBreweryStructure<BukkitDistillery> structure) {
        this(structure, TheBrewingProject.getInstance().getTime());
    }

    public BukkitDistillery(@NotNull PlacedBreweryStructure<BukkitDistillery> structure, long startTime) {
        this.structure = structure;
        this.startTime = startTime;
        BreweryLocation unique = structure.getUnique();
        this.mixture = new BrewInventory(Component.translatable("tbp.distillery.gui-title.distillate"), structure.getStructure().getMeta(StructureMeta.INVENTORY_SIZE), new DistilleryBrewPersistenceHandler(unique, false));
        this.distillate = new BrewInventory(Component.translatable("tbp.distillery.gui-title.mixture"), structure.getStructure().getMeta(StructureMeta.INVENTORY_SIZE), new DistilleryBrewPersistenceHandler(unique, true));
    }

    public boolean open(@NotNull BreweryLocation location, @NotNull UUID playerUuid) {
        checkDirty();
        Player player = Bukkit.getPlayer(playerUuid);
        if (mixtureContainerLocations.contains(location)) {
            playInteractionEffects(location, player);
            return openInventory(mixture, player);
        }
        if (distillateContainerLocations.contains(location)) {
            playInteractionEffects(location, player);
            return openInventory(distillate, player);
        }
        return false;
    }

    @Override
    public void close(boolean silent) {
        Stream.of(mixture, distillate).forEach(inventory -> {
                    inventory.updateBrewsFromInventory();
                    inventory.getInventory().clear();
                }
        );
    }

    private void playInteractionEffects(BreweryLocation location, Player player) {
        BukkitAdapter.toWorld(location)
                .ifPresent(world -> SoundPlayer.playSoundEffect(
                        Config.config().sounds().distilleryAccess(),
                        Sound.Source.BLOCK,
                        world, location.x() + 0.5, location.y() + 0.5, location.z() + 0.5
                ));
        BlockUtil.playWobbleEffect(location, player);
    }

    private boolean openInventory(BrewInventory inventory, Player player) {
        if (!player.hasPermission("brewery.distillery.access")) {
            MessageUtil.message(player, "tbp.distillery.access-denied");
            return false;
        }
        if (inventoryUnpopulated()) {
            mixture.updateInventoryFromBrews();
            distillate.updateInventoryFromBrews();
        }
        this.recentlyAccessed = TheBrewingProject.getInstance().getTime();
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
            MessageUtil.message(player, "tbp.distillery.access-denied");
            return false;
        }
        return inventoryAllows(item);
    }

    @Override
    public boolean inventoryAllows(@NotNull ItemStack item) {
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
            Block block = BukkitAdapter.toBlock(location).orElseThrow();
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

    private boolean inventoryUnpopulated() {
        return recentlyAccessed == -1L || recentlyAccessed + Moment.SECOND <= TheBrewingProject.getInstance().getTime();
    }

    public void tick() {
        BreweryLocation unique = getStructure().getUnique();
        long timeProcessed = getTimeProcessed();
        long processTime = getProcessTime();
        int processedBrews = (int) ((timeProcessed / processTime) * getStructure().getStructure().getMeta(StructureMeta.PROCESS_AMOUNT));
        if (!BlockUtil.isChunkLoaded(unique)
                || mixture.brewAmount() < processedBrews
                || distillate.isFull()) {
            return;
        }
        checkDirty();
        if (timeProcessed % processTime == 0 && timeProcessed != 0) {
            BukkitAdapter.toWorld(unique)
                    .ifPresent(world -> SoundPlayer.playSoundEffect(
                            Config.config().sounds().distilleryProcess(),
                            Sound.Source.BLOCK,
                            world, unique.x() + 0.5, unique.y() + 0.5, unique.z() + 0.5
                    ));
        }
        if (timeProcessed % (processTime / 4) < processTime / 16 && mixture.brewAmount() > processedBrews) {
            distillateContainerLocations.stream()
                    .map(BukkitAdapter::toLocation)
                    .flatMap(Optional::stream)
                    .map(location -> location.add(0.5, 1.3, 0.5))
                    .forEach(location -> location.getWorld().spawnParticle(Particle.ENTITY_EFFECT, location, 2, Color.WHITE));
        }
    }

    public void tickInventory() {
        checkDirty();
        if (inventoryUnpopulated()) {
            close(false);
            TheBrewingProject.getInstance().getBreweryRegistry().unregisterOpened(this);
            // Distilling results can be computed later on
            return;
        }
        if (!mixture.getInventory().getViewers().isEmpty() || !distillate.getInventory().getViewers().isEmpty()) {
            this.recentlyAccessed = TheBrewingProject.getInstance().getTime();
        }
        long timeProcessed = getTimeProcessed();
        long processTime = getProcessTime();
        // Process has changed one extra tick, to avoid running a sound if the mixture inventory changed
        if (timeProcessed < processTime - 1 || mixture.getInventory().isEmpty()) {
            return;
        }
        boolean hasChanged = mixture.updateBrewsFromInventory();
        distillate.updateBrewsFromInventory();
        if (hasChanged) {
            resetStartTime();
            return;
        }
        if (timeProcessed < processTime) {
            return;
        }
        transferItems(mixture, distillate, (int) (getStructure().getStructure().getMeta(StructureMeta.PROCESS_AMOUNT) * (timeProcessed / processTime)));
        distillate.updateInventoryFromBrews();
        mixture.updateInventoryFromBrews();
        resetStartTime();
    }

    @Override
    public Optional<Inventory> access(@NotNull BreweryLocation breweryLocation) {
        if (inventoryUnpopulated()
                && (mixtureContainerLocations.contains(breweryLocation) || distillateContainerLocations.contains(breweryLocation))) {
            mixture.updateInventoryFromBrews();
            distillate.updateInventoryFromBrews();
            TheBrewingProject.getInstance().getBreweryRegistry().registerOpened(this);
        }
        this.recentlyAccessed = TheBrewingProject.getInstance().getTime();
        if (mixtureContainerLocations.contains(breweryLocation)) {
            return Optional.of(mixture.getInventory());
        }
        if (distillateContainerLocations.contains(breweryLocation)) {
            return Optional.of(distillate.getInventory());
        }
        return Optional.empty();
    }

    private long getTimeProcessed() {
        return TheBrewingProject.getInstance().getTime() - startTime;
    }

    private void resetStartTime() {
        startTime = TheBrewingProject.getInstance().getTime();
        try {
            TheBrewingProject.getInstance().getDatabase().updateValue(BukkitDistilleryDataType.INSTANCE, this);
        } catch (PersistenceException e) {
            Logger.logErr(e);
        }
    }

    private long getProcessTime() {
        return getStructure().getStructure().getMeta(StructureMeta.PROCESS_TIME);
    }

    private void transferItems(BrewInventory inventory1, BrewInventory inventory2, int amount) {
        Queue<Pair<Brew, Integer>> brewsToTransfer = new LinkedList<>();
        for (int i = 0; i < inventory1.getBrews().length; i++) {
            if (inventory1.getBrews()[i] == null) {
                continue;
            }
            if (amount-- <= 0) {
                break;
            }
            brewsToTransfer.add(new Pair<>(inventory1.getBrews()[i], i));
        }
        for (int i = 0; i < inventory2.getBrews().length; i++) {
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
                            () -> new DistillStepImpl(1))
                    , i);
        }
    }

    @Override
    public void destroy(BreweryLocation breweryLocation) {
        BukkitAdapter.toLocation(breweryLocation)
                .map(location -> location.add(0.5, 0, 0.5))
                .ifPresent(location -> {
                    for (BrewInventory distilleryInventory : List.of(distillate, mixture)) {
                        List.copyOf(distilleryInventory.getInventory().getViewers()).forEach(HumanEntity::closeInventory);
                        distilleryInventory.getInventory().clear();
                        for (Brew brew : distilleryInventory.getBrews()) {
                            if (brew == null) {
                                continue;
                            }
                            location.getWorld().dropItem(location, BrewAdapter.toItem(brew, new Brew.State.Other()));
                        }
                    }
                });

    }

}
