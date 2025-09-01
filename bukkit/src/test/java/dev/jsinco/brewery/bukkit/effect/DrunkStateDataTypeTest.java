package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockBukkitExtension.class)
class DrunkStateDataTypeTest {


    private Database database;
    private DrunkenModifier alcohol;
    private DrunkenModifier toxins;

    @BeforeEach
    void setup() {
        this.database = MockBukkit.load(TheBrewingProject.class).getDatabase();
        this.alcohol = DrunkenModifierSection.modifiers().modifier("alcohol");
        this.toxins = DrunkenModifierSection.modifiers().modifier("toxins");
    }

    @Test
    void checkPersistence() throws PersistenceException {
        UUID uuid = UUID.randomUUID();
        DrunkStateImpl drunkState = new DrunkStateImpl(10, 20, Map.of(alcohol, 11D, toxins, 22D));
        database.insertValue(SqlDrunkStateDataType.INSTANCE, new Pair<>(drunkState, uuid));
        assertTrue(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).contains(new Pair<>(drunkState, uuid)));
        DrunkStateImpl drunkState2 = new DrunkStateImpl(20, 30, Map.of(alcohol, 22D, toxins, 33D));
        database.updateValue(SqlDrunkStateDataType.INSTANCE, new Pair<>(drunkState2, uuid));
        assertTrue(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).contains(new Pair<>(drunkState2, uuid)));
        assertFalse(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).contains(new Pair<>(drunkState, uuid)));
        database.remove(SqlDrunkStateDataType.INSTANCE, uuid);
        assertTrue(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).isEmpty());
    }

}