package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.testutil.TBPServerMock;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.effect.DrunkenModifierDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrunkStateDataTypeTest {

    private Database database;
    private DrunkenModifier alcohol;
    private DrunkenModifier toxins;

    @BeforeEach
    void setup() {
        MockBukkit.mock(new TBPServerMock());
        this.database = MockBukkit.load(TheBrewingProject.class).getDatabase();
        this.alcohol = DrunkenModifierSection.modifiers().modifier("alcohol");
        this.toxins = DrunkenModifierSection.modifiers().modifier("toxins");
    }
    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void checkPersistence() throws PersistenceException {
        UUID uuid = UUID.randomUUID();

        DrunkStateImpl drunkState = new DrunkStateImpl(10, 20, Map.of(alcohol, 11D, toxins, 22D));
        database.insertValue(SqlDrunkStateDataType.INSTANCE, new Pair<>(drunkState, uuid));
        for (Pair<DrunkenModifierDataType.Data, Double> modifier : toModifiers(drunkState, uuid)) {
            database.insertValue(SqlDrunkenModifierDataType.INSTANCE, modifier);
        }
        assertTrue(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).contains(new Pair<>(drunkState, uuid)));

        DrunkStateImpl drunkState2 = new DrunkStateImpl(20, 30, Map.of(alcohol, 22D, toxins, 33D));
        database.updateValue(SqlDrunkStateDataType.INSTANCE, new Pair<>(drunkState2, uuid));
        for (Pair<DrunkenModifierDataType.Data, Double> modifier : toModifiers(drunkState2, uuid)) {
            database.insertValue(SqlDrunkenModifierDataType.INSTANCE, modifier);
        }
        assertTrue(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).contains(new Pair<>(drunkState2, uuid)));
        assertFalse(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).contains(new Pair<>(drunkState, uuid)));

        database.remove(SqlDrunkStateDataType.INSTANCE, uuid);
        for (Pair<DrunkenModifierDataType.Data, Double> modifier : toModifiers(drunkState, uuid)) {
            database.remove(SqlDrunkenModifierDataType.INSTANCE, modifier.first());
        }
        assertTrue(database.retrieveAllNow(SqlDrunkStateDataType.INSTANCE).isEmpty());
    }

    private List<Pair<DrunkenModifierDataType.Data, Double>> toModifiers(DrunkState drunkState, UUID uuid) {
        return drunkState.modifiers().entrySet().stream()
                .map(en -> {
                    DrunkenModifierDataType.Data data = new DrunkenModifierDataType.Data(en.getKey(), uuid);
                    return new Pair<>(data, en.getValue());
                })
                .toList();
    }

}