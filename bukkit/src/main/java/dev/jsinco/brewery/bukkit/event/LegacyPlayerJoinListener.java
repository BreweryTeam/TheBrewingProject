package dev.jsinco.brewery.bukkit.event;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.DrunksManager;
import dev.jsinco.brewery.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Random;
import java.util.UUID;

public class LegacyPlayerJoinListener implements Listener {
    private final static Random RANDOM = new Random();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        DrunksManager drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        DrunkState drunkState = drunksManager.getDrunkState(playerUuid);
        String playerName = event.getPlayer().getName();
        if (drunksManager.isPassedOut(playerUuid)) {
            String kickEventMessage = EventSection.events().kickEvent().kickEventMessage();
            TagResolver tagResolver = BukkitMessageUtil.getPlayerTagResolver(event.getPlayer());
            Component playerKickMessage = kickEventMessage == null ?
                    Component.translatable("tbp.events.default-kick-event-message", Argument.tagResolver(tagResolver))
                    : MessageUtil.miniMessage(kickEventMessage, tagResolver);
            event.kickMessage(GlobalTranslator.render(playerKickMessage, Config.config().language()));
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            return;
        }
        EventSection.DrunkenJoinEvent joinEvent = EventSection.events().drunkenJoinDeny();
        if (joinEvent.enabled() && drunkState != null && joinEvent.probability().evaluate(drunkState.modifiers()).probability() > RANDOM.nextDouble(100)) {
            event.kickMessage(
                    GlobalTranslator.render(
                            Component.translatable("tbp.events.drunken-join-deny-message", Argument.tagResolver(Placeholder.unparsed("player_name", playerName == null ? "" : playerName))),
                            Config.config().language()
                    )
            );
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        }
    }
}
