package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.ExecutableEventStep;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PassOutNamedExecutable implements ExecutableEventStep {

    @Override
    public void execute(UUID contextPlayer, List<EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return;
        }

        DrunksManagerImpl<?> drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        EventSection.KickEventSection kickEventSection = Config.config().events().kickEvent();
        player.kick(BukkitMessageUtil.compilePlayerMessage(kickEventSection.kickEventMessage() == null ? TranslationsConfig.KICK_EVENT_MESSAGE : kickEventSection.kickEventMessage(), player, drunksManager, 0));
        if (kickEventSection.kickServerMessage() != null) {
            Component message = BukkitMessageUtil.compilePlayerMessage(kickEventSection.kickServerMessage(), player, drunksManager, 0);
            Bukkit.getOnlinePlayers().forEach(player1 -> player1.sendMessage(message));
        }
        drunksManager.registerPassedOut(player.getUniqueId());
    }

}
