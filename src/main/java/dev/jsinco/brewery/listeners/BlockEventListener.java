package dev.jsinco.brewery.listeners;

import dev.jsinco.brewery.structure.BreweryStructure;
import dev.jsinco.brewery.structure.PlacedBreweryStructure;
import dev.jsinco.brewery.structure.StructureRegistry;
import dev.jsinco.brewery.util.Logging;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;
import java.util.Set;

public class BlockEventListener implements Listener {

    private final StructureRegistry structureRegistry;

    public BlockEventListener(StructureRegistry structureRegistry) {
        this.structureRegistry = structureRegistry;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Optional<PlacedBreweryStructure> possibleStructure = getStructure(event.getBlock());
        if (possibleStructure.isEmpty()) {
            return;
        }
        Logging.log("Hello world!"); //TODO: Obviously remove this
    }

    private Optional<PlacedBreweryStructure> getStructure(Block block) {
        Location placedLocation = block.getLocation();
        Material material = block.getType();
        Set<BreweryStructure> possibleStructures = structureRegistry.getPossibleStructures(material);
        for (BreweryStructure structure : possibleStructures) {
            Optional<PlacedBreweryStructure> placedBreweryStructure = PlacedBreweryStructure.findValid(structure, placedLocation);
            if (placedBreweryStructure.isPresent()) {
                return placedBreweryStructure;
            }
        }
        return Optional.empty();
    }
}
