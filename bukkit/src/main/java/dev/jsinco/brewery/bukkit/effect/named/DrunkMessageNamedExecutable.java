package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class DrunkMessageNamedExecutable implements EventPropertyExecutable {

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        List<String> drunkMessages = EventSection.events().drunkMessages();
        if (drunkMessages.isEmpty()) {
            return ExecutionResult.CONTINUE;
        }
        List<Player> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                .filter(Player::isVisibleByDefault)
                .filter(player1 -> !player.equals(player1))
                .map(Player.class::cast)
                .toList();
        if (onlinePlayers.isEmpty()) {
            return ExecutionResult.CONTINUE;
        }
        Player randomPlayer = onlinePlayers.get(RANDOM.nextInt(onlinePlayers.size()));
        player.chat(drunkMessages.get(RANDOM.nextInt(drunkMessages.size())).replace("<random_player_name>", randomPlayer.getName()));
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
