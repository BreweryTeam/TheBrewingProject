package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.api.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockBukkitExtension.class)
class DrunkStateDataTypeTest {


    private Database database;

    @BeforeEach
    void setup() {
        this.database = MockBukkit.load(TheBrewingProject.class).getDatabase();
    }

    @Test
    void checkPersistence() throws PersistenceException {
        UUID uuid = UUID.randomUUID();
        DrunkStateImpl drunkState = new DrunkStateImpl(10, 20, 11, 22);
        database.insertValue(SqlDrunkStateDataType.INSTANCE, new Pair<>(drunkState, uuid));
        assertTrue(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).contains(new Pair<>(drunkState, uuid)));
        DrunkStateImpl drunkState2 = new DrunkStateImpl(20, 30, 22, 33);
        database.updateValue(SqlDrunkStateDataType.INSTANCE, new Pair<>(drunkState2, uuid));
        assertTrue(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).contains(new Pair<>(drunkState2, uuid)));
        assertFalse(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).contains(new Pair<>(drunkState, uuid)));
        database.remove(SqlDrunkStateDataType.INSTANCE, uuid);
        assertTrue(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).isEmpty());
    }

}