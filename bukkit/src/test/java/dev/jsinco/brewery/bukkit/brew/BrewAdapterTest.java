package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.ingredient.SimpleIngredient;
import dev.jsinco.brewery.bukkit.testutil.ItemStackMockPDC;
import dev.jsinco.brewery.bukkit.testutil.TBPServerMock;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrewAdapterTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock(new TBPServerMock());
        MockBukkit.load(TheBrewingProject.class);
    }
    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void test_roundTrip() {
        Brew originalBrew = new BrewImpl(List.of(
                new CookStepImpl(
                        new PassedMoment(20),
                        Map.of(SimpleIngredient.from("wheat").get(), 1),
                        CauldronType.LAVA
                ),
                new DistillStepImpl(
                        3
                ),
                new AgeStepImpl(
                        new PassedMoment(20),
                        BarrelType.ACACIA
                )
        )).withMeta(Key.key("test", "example"), MetaDataType.STRING, "sample text");

        ItemStack item = new ItemStackMockPDC(Material.POTION);
        item.editPersistentDataContainer(pdc -> BrewAdapter.applyBrewData(pdc, originalBrew));
        Brew recreatedBrew = BrewAdapter.fromItem(item).get();

        assertEquals(originalBrew, recreatedBrew);
    }

}
