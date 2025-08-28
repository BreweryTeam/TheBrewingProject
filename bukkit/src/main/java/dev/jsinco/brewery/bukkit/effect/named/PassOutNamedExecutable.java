package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PassOutNamedExecutable implements EventPropertyExecutable {

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null || player.hasPermission("brewery.override.kick")) {
            return ExecutionResult.CONTINUE;
        }

        DrunksManagerImpl<?> drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        EventSection.KickEventSection kickEventSection = EventSection.events().kickEvent();
        Component playerKickMessage = kickEventSection.kickEventMessage() == null ?
                Component.translatable("tbp.events.default-kick-event-message")
                : MessageUtil.miniMessage(kickEventSection.kickEventMessage(), BukkitMessageUtil.getPlayerTagResolver(player));
        player.kick(GlobalTranslator.render(playerKickMessage, Config.config().language()));
        if (kickEventSection.kickServerMessage() != null) {
            Component message = MessageUtil.miniMessage(kickEventSection.kickServerMessage(), BukkitMessageUtil.getPlayerTagResolver(player));
            Bukkit.getOnlinePlayers().forEach(audience -> audience.sendMessage(message));
        }
        drunksManager.registerPassedOut(player.getUniqueId());
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
