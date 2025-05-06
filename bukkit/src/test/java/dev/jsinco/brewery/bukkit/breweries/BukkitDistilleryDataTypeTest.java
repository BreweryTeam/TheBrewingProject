package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.structure.GenericBlockDataMatcher;
import dev.jsinco.brewery.bukkit.structure.PlacedBreweryStructure;
import dev.jsinco.brewery.bukkit.structure.StructurePlacerUtils;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.util.Pair;
import org.bukkit.block.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockBukkitExtension.class)
class BukkitDistilleryDataTypeTest {

    TheBrewingProject plugin;
    Database database;
    @MockBukkitInject
    ServerMock server;
    WorldMock world;

    @BeforeEach
    void setUp() {
        this.plugin = MockBukkit.load(TheBrewingProject.class);
        this.database = plugin.getDatabase();
        this.world = server.addSimpleWorld("world");
    }

    @Test
    void insertAndFetch() throws PersistenceException {
        Block block = world.getBlockAt(0, 1, 0);
        StructurePlacerUtils.constructBambooDistillery(world);
        Optional<Pair<PlacedBreweryStructure<BukkitDistillery>, Void>> optional = PlacedBreweryStructure.findValid(plugin.getStructureRegistry().getStructure("bamboo_distillery").get(), block.getLocation(), GenericBlockDataMatcher.INSTANCE, new Void[1]);
        BukkitDistillery distillery = new BukkitDistillery(optional.get().first(), 100);
        database.insertValue(BukkitDistilleryDataType.INSTANCE, distillery).join();
        List<BukkitDistillery> distilleries = database.findNow(BukkitDistilleryDataType.INSTANCE, world.getUID());
        assertEquals(1, distilleries.size());
        BukkitDistillery found = distilleries.get(0);
        assertEquals(distillery.getStructure().getStructure().getName(), found.getStructure().getStructure().getName());
        assertEquals(distillery.getStructure().getTransformation(), found.getStructure().getTransformation());
        assertEquals(100, found.getStartTime());
        BukkitDistillery newDistillery = new BukkitDistillery(optional.get().first(), 200);
        database.updateValue(BukkitDistilleryDataType.INSTANCE, newDistillery).join();
        List<BukkitDistillery> fetchWithUpdatedValue = database.findNow(BukkitDistilleryDataType.INSTANCE, world.getUID());
        assertEquals(1, distilleries.size());
        assertEquals(newDistillery.getStructure().getStructure().getName(), fetchWithUpdatedValue.getFirst().getStructure().getStructure().getName());
        assertEquals(newDistillery.getStructure().getTransformation(), fetchWithUpdatedValue.getFirst().getStructure().getTransformation());
        assertEquals(200, fetchWithUpdatedValue.get(0).getStartTime());
        database.remove(BukkitDistilleryDataType.INSTANCE, newDistillery);
        assertTrue(database.findNow(BukkitDistilleryDataType.INSTANCE, world.getUID()).isEmpty());
    }
}