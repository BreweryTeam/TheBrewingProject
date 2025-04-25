package dev.jsinco.brewery.bukkit.listeners;

import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.InventoryAccessible;
import dev.jsinco.brewery.breweries.StructureHolder;
import dev.jsinco.brewery.bukkit.breweries.*;
import dev.jsinco.brewery.bukkit.structure.*;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.structure.MultiblockStructure;
import dev.jsinco.brewery.structure.PlacedStructureRegistryImpl;
import dev.jsinco.brewery.structure.StructureMeta;
import dev.jsinco.brewery.structure.StructureType;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.vector.BreweryLocation;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.*;
import java.util.stream.Collectors;

public class BlockEventListener implements Listener {

    private final StructureRegistry structureRegistry;
    private final PlacedStructureRegistryImpl placedStructureRegistry;
    private final Database database;
    private final BreweryRegistry breweryRegistry;
    private final Set<Material> trackedDistilleryBlocks;

    public BlockEventListener(StructureRegistry structureRegistry, PlacedStructureRegistryImpl placedStructureRegistry, Database database, BreweryRegistry breweryRegistry) {
        this.structureRegistry = structureRegistry;
        this.placedStructureRegistry = placedStructureRegistry;
        this.database = database;
        this.breweryRegistry = breweryRegistry;
        this.trackedDistilleryBlocks = structureRegistry.getStructures(StructureType.DISTILLERY)
                .stream()
                .map(structure -> structure.getMeta(StructureMeta.TAGGED_MATERIAL))
                .map(string -> string.toUpperCase(Locale.ROOT))
                .map(Material::valueOf)
                .collect(Collectors.toSet());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChangeEvent(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (!"barrel".equalsIgnoreCase(lines[0]) || !lines[1].isEmpty() || !lines[2].isEmpty() || !lines[3].isEmpty()) {
            return;
        }
        if (!(event.getBlock().getBlockData() instanceof WallSign wallSign)) {
            return;
        }
        Optional<Pair<PlacedBreweryStructure<BukkitBarrel>, BarrelType>> possibleStructure = getBarrel(event.getBlock().getRelative(wallSign.getFacing().getOppositeFace()));
        if (possibleStructure.isEmpty()) {
            return;
        }
        Pair<PlacedBreweryStructure<BukkitBarrel>, BarrelType> placedStructurePair = possibleStructure.get();
        PlacedBreweryStructure<BukkitBarrel> placedBreweryStructure = placedStructurePair.first();
        if (!placedStructureRegistry.getStructures(placedBreweryStructure.positions()).isEmpty()) {
            // Exit if there's an overlapping structure
            return;
        }
        if (!event.getPlayer().hasPermission("brewery.barrel.create")) {
            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.BARREL_CREATE_DENIED));
            return;
        }
        event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.BARREL_CREATE));
        placedStructureRegistry.registerStructure(placedBreweryStructure);
        BukkitBarrel barrel = new BukkitBarrel(event.getBlock().getLocation(), placedBreweryStructure, placedBreweryStructure.getStructure().getMeta(StructureMeta.INVENTORY_SIZE), placedStructurePair.second());
        placedBreweryStructure.setHolder(barrel);
        breweryRegistry.registerInventory(barrel);
        try {
            database.insertValue(BukkitBarrelDataType.INSTANCE, barrel);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent placeEvent) {
        Block placed = placeEvent.getBlockPlaced();
        if (trackedDistilleryBlocks.contains(placed.getType())) {
            for (BreweryStructure breweryStructure : structureRegistry.getPossibleStructures(placed.getType(), StructureType.DISTILLERY)) {
                Optional<Pair<PlacedBreweryStructure<BukkitDistillery>, Void>> placedBreweryStructureOptional = PlacedBreweryStructure.findValid(breweryStructure, placed.getLocation(), GenericBlockDataMatcher.INSTANCE, new Void[1]);
                if (placedBreweryStructureOptional.isPresent()) {
                    if (!placedStructureRegistry.getStructures(placedBreweryStructureOptional.get().first().positions()).isEmpty()) {
                        continue;
                    }
                    if (!placeEvent.getPlayer().hasPermission("brewery.distillery.create")) {
                        placeEvent.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.DISTILLERY_CREATE_DENIED));
                        return;
                    }
                    registerDistillery(placedBreweryStructureOptional.get().first());
                    placeEvent.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.DISTILLERY_CREATE));
                    return;
                }
            }
        }
    }

    private void registerDistillery(PlacedBreweryStructure<BukkitDistillery> distilleryPlacedBreweryStructure) {
        BukkitDistillery bukkitDistillery = new BukkitDistillery(distilleryPlacedBreweryStructure);
        distilleryPlacedBreweryStructure.setHolder(bukkitDistillery);
        placedStructureRegistry.registerStructure(distilleryPlacedBreweryStructure);
        try {
            database.insertValue(BukkitDistilleryDataType.INSTANCE, bukkitDistillery);
            breweryRegistry.registerInventory(bukkitDistillery);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    private Optional<Pair<PlacedBreweryStructure<BukkitBarrel>, BarrelType>> getBarrel(Block block) {
        Location placedLocation = block.getLocation();
        Material material = block.getType();
        Set<BreweryStructure> possibleStructures = structureRegistry.getPossibleStructures(material, StructureType.BARREL);
        for (BreweryStructure structure : possibleStructures) {
            Optional<Pair<PlacedBreweryStructure<BukkitBarrel>, BarrelType>> placedBreweryStructure = PlacedBreweryStructure.findValid(structure, placedLocation, BarrelBlockDataMatcher.INSTANCE, BarrelType.PLACEABLE_TYPES);
            if (placedBreweryStructure.isPresent()) {
                return placedBreweryStructure;
            }
        }
        return Optional.empty();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        destroyFromBlock(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        onMultiBlockRemove(event.getBlocks().stream()
                .map(Block::getLocation)
                .toList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        onMultiBlockRemove(event.getBlocks().stream()
                .map(Block::getLocation)
                .toList()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        onMultiBlockRemove(event.blockList().stream()
                .map(Block::getLocation)
                .toList()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        onMultiBlockRemove(event.blockList().stream()
                .map(Block::getLocation)
                .toList()
        );
    }

    private void onMultiBlockRemove(List<Location> locationList) {
        Set<MultiblockStructure<?>> multiblockStructures = new HashSet<>();
        Set<StructureHolder<?>> holders = new HashSet<>();
        for (Location location : locationList) {
            BreweryLocation breweryLocation = BukkitAdapter.toBreweryLocation(location);
            placedStructureRegistry.getHolder(breweryLocation).ifPresent(holder -> {
                holders.add(holder);
                multiblockStructures.add(holder.getStructure());
                if (holder instanceof InventoryAccessible inventoryAccessible) {
                    breweryRegistry.unregisterInventory(inventoryAccessible);
                }
            });
            breweryRegistry.getActiveSinglePositionStructure(breweryLocation).ifPresent(cauldron -> ListenerUtil.removeActiveSinglePositionStructure(cauldron, breweryRegistry, database));
        }
        multiblockStructures.forEach(placedStructureRegistry::unregisterStructure);
        for (StructureHolder<?> holder : holders) {
            holder.destroy(BukkitAdapter.toBreweryLocation(locationList.getFirst()));
            remove(holder);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        destroyFromBlock(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        destroyFromBlock(event.getBlock());
    }

    /**
     * Assumes only one block has changed in the event, is not safe to use in multi-block changes
     *
     * @param block
     */
    private void destroyFromBlock(Block block) {
        BreweryLocation breweryLocation = BukkitAdapter.toBreweryLocation(block);
        Optional<MultiblockStructure<?>> multiBlockStructure = placedStructureRegistry.getStructure(breweryLocation);
        multiBlockStructure.ifPresent(placedStructureRegistry::unregisterStructure);
        multiBlockStructure
                .map(MultiblockStructure::getHolder)
                .ifPresent(holder -> {
                    holder.destroy(breweryLocation);
                    remove(holder);
                    if (holder instanceof InventoryAccessible inventoryAccessible) {
                        breweryRegistry.unregisterInventory(inventoryAccessible);
                    }
                });
        breweryRegistry.getActiveSinglePositionStructure(breweryLocation).ifPresent(cauldron -> ListenerUtil.removeActiveSinglePositionStructure(cauldron, breweryRegistry, database));
    }

    private void remove(StructureHolder<?> holder) {
        try {
            if (holder instanceof BukkitBarrel barrel) {
                database.remove(BukkitBarrelDataType.INSTANCE, barrel);
            }
            if (holder instanceof BukkitDistillery distillery) {
                database.remove(BukkitDistilleryDataType.INSTANCE, distillery);
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }
}
