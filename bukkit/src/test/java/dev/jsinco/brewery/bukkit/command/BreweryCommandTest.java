package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}