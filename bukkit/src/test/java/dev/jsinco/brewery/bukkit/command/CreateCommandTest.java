package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.bukkit.command.TabCompleter;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockBukkitExtension.class)
class CreateCommandTest {

    @MockBukkitInject
    ServerMock serverMock;
    TheBrewingProject theBrewingProject;
    PlayerMock target;

    @BeforeEach
    void setUp() {
        theBrewingProject = MockBukkit.load(TheBrewingProject.class);
        target = serverMock.addPlayer();
        target.addAttachment(theBrewingProject, "brewery.command", true);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/command/create_command_invalid_args.csv")
    void onCommand(String arguments) {
        assertDoesNotThrow(() -> CreateCommand.onCommand(target, target, arguments.split(" ")));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/command/create_command_invalid_args.csv")
    void tabComplete(String arguments) {
        assertDoesNotThrow(() -> CreateCommand.tabComplete(arguments.split(" ")));
    }
}