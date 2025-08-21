package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.util.FutureUtil;
import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockBukkitExtension.class)
class BukkitBukkitCauldronDataTypeTest {
    @MockBukkitInject
    ServerMock server;
    private @NotNull WorldMock world;
    private Database database;

    @BeforeEach
    void setUp() {
        this.world = server.addSimpleWorld("hello!");
        TheBrewingProject theBrewingProject = MockBukkit.load(TheBrewingProject.class);
        this.database = theBrewingProject.getDatabase();
    }

    @Test
    void checkPersistence() throws PersistenceException {
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.WATER_CAULDRON);
        BreweryLocation position = BukkitAdapter.toBreweryLocation(block);
        BukkitCauldron cauldron = new BukkitCauldron(position, true);
        database.insertValue(BukkitCauldronDataType.INSTANCE, cauldron);
        List<BukkitCauldron> cauldrons = FutureUtil.mergeFutures(database.findNow(BukkitCauldronDataType.INSTANCE, world.getUID())).join();
        assertEquals(1, cauldrons.size());
        BukkitCauldron retrievedCauldron = cauldrons.get(0);
        assertEquals(cauldron.getBrew(), retrievedCauldron.getBrew());
        assertEquals(cauldron.position(), retrievedCauldron.position());
        BukkitCauldron updatedValue = new BukkitCauldron(position, true);
        database.updateValue(BukkitCauldronDataType.INSTANCE, updatedValue);
        List<BukkitCauldron> updatedCauldrons = FutureUtil.mergeFutures(database.findNow(BukkitCauldronDataType.INSTANCE, world.getUID())).join();
        assertEquals(1, updatedCauldrons.size());
        database.remove(BukkitCauldronDataType.INSTANCE, cauldron);
        assertEquals(0, database.findNow(BukkitCauldronDataType.INSTANCE, world.getUID()).size());
    }
}