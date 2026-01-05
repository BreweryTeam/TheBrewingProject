package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.api.breweries.InventoryAccessible;
import dev.jsinco.brewery.api.breweries.StructureHolder;
import dev.jsinco.brewery.api.structure.*;
import dev.jsinco.brewery.api.util.CancelState;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.bukkit.api.event.BarrelDestroyEvent;
import dev.jsinco.brewery.bukkit.api.event.CauldronDestroyEvent;
import dev.jsinco.brewery.bukkit.api.event.DistilleryDestroyEvent;
import dev.jsinco.brewery.bukkit.api.event.PermissibleBreweryEvent;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrelDataType;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistilleryDataType;
import dev.jsinco.brewery.bukkit.structure.*;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.structure.PlacedStructureRegistryImpl;
import dev.jsinco.brewery.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ExplosionResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.HopperInventorySearchEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockEventListener implements Listener {

    private final StructureRegistry structureRegistry;
    private final PlacedStructureRegistryImpl placedStructureRegistry;
    private final Database database;
    private final BreweryRegistry breweryRegistry;

    public BlockEventListener(StructureRegistry structureRegistry, PlacedStructureRegistryImpl placedStructureRegistry, Database database, BreweryRegistry breweryRegistry) {
        this.structureRegistry = structureRegistry;
        this.placedStructureRegistry = placedStructureRegistry;
        this.database = database;
        this.breweryRegistry = breweryRegistry;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChangeEvent(SignChangeEvent event) {

        Set<String> keywords = Config.config().barrels().signKeywords().stream().map(String::toLowerCase).collect(Collectors.toSet());
        String firstLine = PlainTextComponentSerializer.plainText().serialize(event.lines().getFirst()).toLowerCase();
        if (Config.config().barrels().requireSignKeyword() && !keywords.contains(firstLine)) {
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
            MessageUtil.message(event.getPlayer(), "tbp.barrel.create-denied");
            return;
        }
        MessageUtil.message(event.getPlayer(), "tbp.barrel.create");
        BukkitBarrel barrel = new BukkitBarrel(BukkitAdapter.toLocation(placedBreweryStructure.getUnique()).orElseThrow(), placedBreweryStructure, placedBreweryStructure.getStructure().getMeta(StructureMeta.INVENTORY_SIZE), placedStructurePair.second());
        placedBreweryStructure.setHolder(barrel);
        placedStructureRegistry.registerStructure(placedBreweryStructure);
        breweryRegistry.registerInventory(barrel);
        try {
            database.insertValue(BukkitBarrelDataType.INSTANCE, barrel);
        } catch (PersistenceException e) {
            Logger.logErr(e);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent placeEvent) {
        Block placed = placeEvent.getBlockPlaced();
        for (BreweryStructure breweryStructure : structureRegistry.getPossibleStructures(placed.getType(), StructureType.DISTILLERY)) {
            Optional<Pair<PlacedBreweryStructure<BukkitDistillery>, Void>> placedBreweryStructureOptional = PlacedBreweryStructure.findValid(
                    breweryStructure,
                    placed.getLocation(),
                    new GenericBlockDataMatcher(breweryStructure.getMetaOrDefault(StructureMeta.BLOCK_REPLACEMENTS, new BlockMatcherReplacement.List()).elements()),
                    new Void[1]
            );
            if (placedBreweryStructureOptional.isPresent()) {
                if (!placedStructureRegistry.getStructures(placedBreweryStructureOptional.get().first().positions()).isEmpty()) {
                    continue;
                }

                Player player = placeEvent.getPlayer();
                if (!player.hasPermission("brewery.distillery.create")) {
                    MessageUtil.message(player, "tbp.distillery.create-denied");
                    return;
                }
                registerDistillery(placedBreweryStructureOptional.get().first());
                MessageUtil.message(player, "tbp.distillery.create");
                return;
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
            Logger.logErr(e);
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
        boolean success = destroyFromBlock(event.getBlock(), event.getPlayer());
        if (!success) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        boolean success = onMultiBlockRemove(event.getBlocks().stream()
                .map(Block::getLocation)
                .toList(), null);
        if (!success) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        boolean success = onMultiBlockRemove(event.getBlocks().stream()
                .map(Block::getLocation)
                .toList(), null);
        if (!success) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getExplosionResult() == ExplosionResult.TRIGGER_BLOCK) {
            return;
        }
        boolean success = onMultiBlockRemove(event.blockList().stream()
                .map(Block::getLocation)
                .toList(), null);
        if (!success) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.getExplosionResult() == ExplosionResult.TRIGGER_BLOCK) {
            return;
        }
        boolean success = onMultiBlockRemove(event.blockList().stream()
                .map(Block::getLocation)
                .toList(), null);
        if (!success) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        boolean success = destroyFromBlock(event.getBlock(), null);
        if (!success) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Player player = event.getEntity() instanceof Player p ? p : null;
        boolean success = destroyFromBlock(event.getBlock(), player);
        if (!success) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHopperInventorySearch(HopperInventorySearchEvent event) {
        Block searchBlock = event.getSearchBlock();
        BreweryLocation breweryLocation = BukkitAdapter.toBreweryLocation(searchBlock);
        Optional<InventoryAccessible<ItemStack, Inventory>> inventoryAccessibleOptional = placedStructureRegistry.getStructure(breweryLocation)
                .map(MultiblockStructure::getHolder)
                .filter(InventoryAccessible.class::isInstance)
                .map(inventoryAccessible -> (InventoryAccessible<ItemStack, Inventory>) inventoryAccessible);
        if (!Config.config().automation()) {
            inventoryAccessibleOptional.ifPresent(ignored -> event.setInventory(null));
            return;
        }
        inventoryAccessibleOptional
                .flatMap(inventoryAccessible -> inventoryAccessible.access(breweryLocation))
                .ifPresent(event::setInventory);
    }

    /**
     * Assumes only one block has changed in the event, is not safe to use in multi-block changes
     *
     * @param block
     */
    private boolean destroyFromBlock(Block block, @Nullable Player player) {
        return onMultiBlockRemove(List.of(block.getLocation()), player);
    }

    private boolean onMultiBlockRemove(List<Location> locations, @Nullable Player player) {
        Set<SinglePositionStructure> singlePositionStructures = new HashSet<>();
        Set<MultiblockStructure<?>> multiblockStructures = new HashSet<>();
        Set<StructureHolder<?>> holders = new HashSet<>();
        for (Location location : locations) {
            BreweryLocation breweryLocation = BukkitAdapter.toBreweryLocation(location);
            boolean cancelled = breweryRegistry.getActiveSinglePositionStructure(breweryLocation).map(structure -> {
                CancelState state = callSinglePositionStructureEvent(location, player, structure);
                if (state instanceof CancelState.Allowed) {
                    singlePositionStructures.add(structure);
                    return false;
                } else {
                    if (player != null && state instanceof CancelState.PermissionDenied(Component message)) {
                        player.sendMessage(message);
                    }
                    return true;
                }
            }).orElse(false);
            if (cancelled) {
                return false;
            }
            cancelled = placedStructureRegistry.getHolder(breweryLocation).map(holder -> {
                if (holders.contains(holder)) {
                    return false;
                }
                CancelState state = callPlacedStructureEvent(location, player, holder);
                if (state instanceof CancelState.Allowed) {
                    holders.add(holder);
                    multiblockStructures.add(holder.getStructure());
                    return false;
                } else {
                    if (player != null && state instanceof CancelState.PermissionDenied(Component message)) {
                        player.sendMessage(message);
                    }
                    return true;
                }
            }).orElse(false);
            if (cancelled) {
                return false;
            }
        }
        singlePositionStructures.forEach(structure -> ListenerUtil.removeActiveSinglePositionStructure(structure, breweryRegistry, database));
        multiblockStructures.forEach(placedStructureRegistry::unregisterStructure);
        for (StructureHolder<?> holder : holders) {
            if (holder instanceof InventoryAccessible inventoryAccessible) {
                breweryRegistry.unregisterInventory(inventoryAccessible);
            }
            holder.destroy(BukkitAdapter.toBreweryLocation(locations.getFirst()));
            remove(holder);
        }
        return true;
    }

    private static CancelState callSinglePositionStructureEvent(Location location, @Nullable Player player, SinglePositionStructure structure) {
        if (structure instanceof Cauldron cauldron) {
            CauldronDestroyEvent event = new CauldronDestroyEvent(
                    player == null || player.hasPermission("brewery.cauldron.access") ?
                            new CancelState.Allowed() :
                            new CancelState.PermissionDenied(Component.translatable("tbp.cauldron.access-denied")),
                    cauldron,
                    player,
                    location
            );
            event.callEvent();
            return event.getCancelState();
        }
        return new CancelState.Allowed();
    }

    private static CancelState callPlacedStructureEvent(Location location, @Nullable Player player, StructureHolder<?> holder) {
        PermissibleBreweryEvent event = switch (holder) {
            case BukkitBarrel barrel -> new BarrelDestroyEvent(
                    player == null || player.hasPermission("brewery.barrel.access") ?
                            new CancelState.Allowed() :
                            new CancelState.PermissionDenied(Component.translatable("tbp.barrel.access-denied")),
                    barrel,
                    player,
                    location
            );
            case BukkitDistillery distillery -> new DistilleryDestroyEvent(
                    player == null || player.hasPermission("brewery.distillery.access") ?
                            new CancelState.Allowed() :
                            new CancelState.PermissionDenied(Component.translatable("tbp.distillery.access-denied")),
                    distillery,
                    player,
                    location
            );
            default -> null;
        };
        if (event != null) {
            event.callEvent();
            return event.getCancelState();
        }
        return new CancelState.Allowed();
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
            Logger.logErr(e);
        }
    }
}
