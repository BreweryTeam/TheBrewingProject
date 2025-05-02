package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;

@ExtendWith(MockBukkitExtension.class)
class BreweryCommandTest {

    @MockBukkitInject
    ServerMock serverMock;
    Player target;
    TheBrewingProject theBrewingProject;

    @BeforeEach
    void setUp() {
        theBrewingProject = MockBukkit.load(TheBrewingProject.class);
        target = serverMock.addPlayer();
        target.addAttachment(theBrewingProject, "brewery.command", true);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/command/brew_command_invalid_args.csv")
    void onCommand(String arguments) {
        target.performCommand(arguments);
    }
}