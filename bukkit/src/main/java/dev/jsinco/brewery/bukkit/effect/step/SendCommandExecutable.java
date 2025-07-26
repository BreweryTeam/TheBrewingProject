package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.event.EventPropertyExecutable;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.step.SendCommand.CommandSenderType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class SendCommandExecutable implements EventPropertyExecutable {

    private final String command;
    private final CommandSenderType senderType;

    public SendCommandExecutable(String command, CommandSenderType senderType) {
        this.command = command;
        this.senderType = senderType;
    }

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }
        String command = this.command.replace("@player_name@", player.getName());
        switch (senderType) {
            case PLAYER -> player.performCommand(command);
            case SERVER -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return 4;
    }

}
