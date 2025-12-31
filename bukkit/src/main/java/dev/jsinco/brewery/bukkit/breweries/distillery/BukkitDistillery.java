package dev.jsinco.brewery.bukkit.breweries.distillery;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.Distillery;
import dev.jsinco.brewery.api.breweries.DistilleryAccess;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.structure.MaterialTag;
import dev.jsinco.brewery.api.structure.StructureMeta;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.breweries.BrewInventoryImpl;
import dev.jsinco.brewery.bukkit.structure.BreweryStructure;
import dev.jsinco.brewery.bukkit.structure.PlacedBreweryStructure;
import dev.jsinco.brewery.bukkit.util.BlockUtil;
import dev.jsinco.brewery.bukkit.util.SoundPlayer;
import dev.jsinco.brewery.bukkit.util.VectorUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.util.MessageUtil;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BukkitDistillery implements Distillery<BukkitDistillery, ItemStack, Inventory>, DistilleryAccess {

    @Getter
    private final PlacedBreweryStructure<BukkitDistillery> structure;
    @Getter
    private long startTime;
    @Getter
    private final BrewInventoryImpl mixture;
    @Getter
    private final BrewInventoryImpl distillate;
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
        this.mixture = new BrewInventoryImpl(Component.translatable("tbp.distillery.gui-title.mixture"), structure.getStructure().getMeta(StructureMeta.INVENTORY_SIZE), new DistilleryBrewPersistenceHandler(unique, false));
        this.distillate = new BrewInventoryImpl(Component.translatable("tbp.distillery.gui-title.distillate"), structure.getStructure().getMeta(StructureMeta.INVENTORY_SIZE), new DistilleryBrewPersistenceHandler(unique, true));
    }

    @Override
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

    private boolean openInventory(BrewInventoryImpl inventory, Player player) {
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
        BreweryStructure breweryStructure = structure.getStructure();

        // Locate key structure parts from reading from the world, try to prioritize large features first
        if (breweryStructure.hasMeta(StructureMeta.DISTILLATE_MATERIAL_TAG)) {
            MaterialTag distillateMaterialTag = breweryStructure.getMeta(StructureMeta.DISTILLATE_MATERIAL_TAG);
            if (breweryStructure.hasMeta(StructureMeta.MIXTURE_MATERIAL_TAG)) {
                MaterialTag mixtureMaterialTag = breweryStructure.getMeta(StructureMeta.MIXTURE_MATERIAL_TAG);
                if (mixtureMaterialTag.volume() > distillateMaterialTag.volume()) {
                    List<BreweryLocation> mixturePositions = findMaterialRegion(mixtureMaterialTag, List.of());
                    mixtureContainerLocations.addAll(mixturePositions);
                    distillateContainerLocations.addAll(findMaterialRegion(distillateMaterialTag, mixturePositions));
                } else {
                    List<BreweryLocation> distillatePositions = findMaterialRegion(distillateMaterialTag, List.of());
                    distillateContainerLocations.addAll(distillatePositions);
                    mixtureContainerLocations.addAll(findMaterialRegion(mixtureMaterialTag, distillatePositions));
                }
                return;
            }
            distillateContainerLocations.addAll(findMaterialRegion(distillateMaterialTag, List.of()));
        }
        if (breweryStructure.hasMeta(StructureMeta.MIXTURE_MATERIAL_TAG)) {
            MaterialTag mixtureMaterialTag = breweryStructure.getMeta(StructureMeta.MIXTURE_MATERIAL_TAG);
            mixtureContainerLocations.addAll(findMaterialRegion(mixtureMaterialTag, List.of()));
        }
        BreweryLocation worldOrigin = BukkitAdapter.toBreweryLocation(structure.getWorldOrigin());
        if (breweryStructure.hasMeta(StructureMeta.DISTILLATE_ACCESS_POINTS)) {
            breweryStructure.getMeta(StructureMeta.DISTILLATE_ACCESS_POINTS)
                    .elements()
                    .stream()
                    .map(VectorUtil::toJoml)
                    .map(vector -> VectorUtil.transform(vector, structure.getTransformation()))
                    .map(VectorUtil::toBreweryVector)
                    .map(worldOrigin::add)
                    .forEach(distillateContainerLocations::add);
        }
        if (breweryStructure.hasMeta(StructureMeta.MIXTURE_ACCESS_POINTS)) {
            breweryStructure.getMeta(StructureMeta.MIXTURE_ACCESS_POINTS)
                    .elements()
                    .stream()
                    .map(VectorUtil::toJoml)
                    .map(vector -> VectorUtil.transform(vector, structure.getTransformation()))
                    .map(VectorUtil::toBreweryVector)
                    .map(worldOrigin::add)
                    .forEach(mixtureContainerLocations::add);
        }
    }

    private List<BreweryLocation> findMaterialRegion(MaterialTag tag, List<BreweryLocation> blackList) {
        Set<Material> materials = tag.materials().stream()
                .map(BukkitAdapter::toMaterial)
                .collect(Collectors.toSet());
        List<BreweryLocation> matchingPositions = structure.positions().stream()
                .map(BukkitAdapter::toBlock)
                .flatMap(Optional::stream)
                .filter(block -> materials.contains(block.getType()))
                .map(BukkitAdapter::toBreweryLocation)
                .toList();
        List<BreweryLocation> output = new ArrayList<>();
        Vector3i region = new Vector3i(tag.xRegion(), tag.yRegion(), tag.zRegion());
        Vector3i transformedRegion = VectorUtil.transform(region, structure.getTransformation());
        for (BreweryLocation matchingPosition : matchingPositions) {
            List<BreweryLocation> found = findInSelection(matchingPosition, transformedRegion, matchingPositions);
            if (blackList.stream().anyMatch(found::contains)) {
                continue;
            }
            output.addAll(found);
        }
        return output;
    }

    private List<BreweryLocation> findInSelection(BreweryLocation startingPoint, Vector3i region, List<BreweryLocation> matchingPositions) {
        List<BreweryLocation> output = new ArrayList<>();
        for (int dx = 0; dx < Math.abs(region.x()); dx++) {
            for (int dy = 0; dy < Math.abs(region.y()); dy++) {
                for (int dz = 0; dz < Math.abs(region.z()); dz++) {
                    BreweryLocation relative = startingPoint.add(dx, dy, dz);
                    if (!matchingPositions.contains(relative)) {
                        return List.of();
                    }
                    output.add(relative);
                }
            }
        }
        return output;
    }

    private boolean shouldUnpopulateInventory() {
        return recentlyAccessed == -1L || recentlyAccessed + Moment.SECOND <= TheBrewingProject.getInstance().getTime();
    }

    private boolean inventoryUnpopulated() {
        return recentlyAccessed == -1L;
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
        long particleEffectInterval = Math.max(processTime / 4L, 10L);
        if (timeProcessed % particleEffectInterval < 5 && mixture.brewAmount() > processedBrews) {
            distillateContainerLocations.stream()
                    .map(BukkitAdapter::toLocation)
                    .flatMap(Optional::stream)
                    .map(location -> location.add(0.5, 1.3, 0.5))
                    .forEach(location -> location.getWorld().spawnParticle(Particle.ENTITY_EFFECT, location, 2, Color.WHITE));
        }
    }

    public void tickInventory() {
        checkDirty();
        if (shouldUnpopulateInventory()) {
            close(false);
            TheBrewingProject.getInstance().getBreweryRegistry().unregisterOpened(this);
            // Distilling results can be computed later on
            this.recentlyAccessed = -1L;
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

    private void transferItems(BrewInventoryImpl inventory1, BrewInventoryImpl inventory2, int amount) {
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
                            BrewingStep.Distill::incrementRuns,
                            () -> new DistillStepImpl(1))
                    , i);
        }
    }

    @Override
    public void destroy(BreweryLocation breweryLocation) {
        BukkitAdapter.toLocation(breweryLocation)
                .map(location -> location.add(0.5, 0, 0.5))
                .ifPresent(location -> {
                    boolean inventoryUnpopulated = inventoryUnpopulated();
                    for (BrewInventoryImpl distilleryInventory : List.of(distillate, mixture)) {
                        if (!inventoryUnpopulated) {
                            distilleryInventory.updateBrewsFromInventory();
                        }
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
