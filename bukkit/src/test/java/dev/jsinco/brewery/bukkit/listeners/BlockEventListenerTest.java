package dev.jsinco.brewery.bukkit.listeners;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistilleryDataType;
import dev.jsinco.brewery.bukkit.structure.StructurePlacerUtils;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.database.PersistenceException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.simulate.entity.PlayerSimulation;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockBukkitExtension.class)
public class BlockEventListenerTest {

    @MockBukkitInject
    ServerMock serverMock;
    TheBrewingProject plugin;
    BlockEventListener blockListener;
    WorldMock world;
    private PlayerMock player;

    @BeforeEach
    void setup() {
        this.world = serverMock.addSimpleWorld("world");
        this.plugin = MockBukkit.load(TheBrewingProject.class);
        this.blockListener = new BlockEventListener(plugin.getStructureRegistry(), plugin.getPlacedStructureRegistry(), plugin.getDatabase(), plugin.getBreweryRegistry());
        this.player = serverMock.addPlayer();
    }

    @Test
    void distilleryTest() throws PersistenceException {
        StructurePlacerUtils.constructBambooDistillery(world);
        PlayerSimulation playerSimulation = new PlayerSimulation(player);
        Location potLocation = new Location(world, 0, 1, 0);
        playerSimulation.simulateBlockPlace(Material.DECORATED_POT, potLocation);
        assertTrue(plugin.getPlacedStructureRegistry().getStructure(BukkitAdapter.toBreweryLocation(potLocation)).isPresent());
        assertEquals(1, plugin.getDatabase().findNow(BukkitDistilleryDataType.INSTANCE, world.getUID()).size());
        playerSimulation.simulateBlockBreak(potLocation.getBlock());
        assertFalse(plugin.getPlacedStructureRegistry().getStructure(BukkitAdapter.toBreweryLocation(potLocation)).isPresent());
        assertEquals(0, plugin.getDatabase().findNow(BukkitDistilleryDataType.INSTANCE, world.getUID()).size());
    }
}
