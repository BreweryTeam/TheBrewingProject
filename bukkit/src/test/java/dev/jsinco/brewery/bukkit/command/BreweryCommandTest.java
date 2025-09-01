package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.api.effect.DrunkState;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockBukkitExtension.class)
class BreweryCommandTest {

    @MockBukkitInject
    ServerMock serverMock;
    PlayerMock target;
    TheBrewingProject theBrewingProject;

    @BeforeEach
    void setUp() {
        theBrewingProject = MockBukkit.load(TheBrewingProject.class);
        target = serverMock.addPlayer();
        target.addAttachment(theBrewingProject, "brewery.command", true);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/command/brew_command_invalid_args.csv")
    void onCommand_invalid(String command) {
        assertDoesNotThrow(() -> target.performCommand(command));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/command/create_command_invalid_args.csv")
    void onCreateCommand_invalid(String command) {
        assertDoesNotThrow(() -> target.performCommand(command));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/command/create_command_valid.csv")
    void onCreateCommand_valid(String command) {
        target.performCommand(command);
        assertEquals(Material.POTION, target.getInventory().getItemInMainHand().getType(), target.nextMessage());
    }

    @Test
    void setStatus() {
        assertDoesNotThrow(() -> target.performCommand("tbp status info"));
        target.performCommand("tbp status consume 30 40");
        DrunkState drunkState = TheBrewingProject.getInstance().getDrunksManager().getDrunkState(target.getUniqueId());
        assertEquals(30, drunkState.modifierValue("alcohol"));
        assertEquals(40, drunkState.modifierValue("toxins"));
        target.performCommand("tbp status set 10 20");
        assertDoesNotThrow(() -> target.performCommand("tbp status info"));
        drunkState = TheBrewingProject.getInstance().getDrunksManager().getDrunkState(target.getUniqueId());
        assertEquals(10, drunkState.modifierValue("alcohol"));
        assertEquals(20, drunkState.modifierValue("toxins"));
        target.performCommand("tbp status clear");
        drunkState = TheBrewingProject.getInstance().getDrunksManager().getDrunkState(target.getUniqueId());
        assertNull(drunkState);
    }
}