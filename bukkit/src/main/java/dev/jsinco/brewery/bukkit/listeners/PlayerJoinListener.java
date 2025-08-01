package dev.jsinco.brewery.bukkit.listeners;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.effect.DrunkState;
import dev.jsinco.brewery.effect.DrunksManager;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerLoginConnection;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Random;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final static Random RANDOM = new Random();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerConnectionValidateLoginEvent event) {
        PlayerProfile profile = null;
        if (event.getConnection() instanceof PlayerLoginConnection connection) {
            profile = connection.getAuthenticatedProfile();
        } else if (event.getConnection() instanceof PlayerConfigurationConnection connection) {
            profile = connection.getProfile();
        }
        if (profile == null) {
            return;
        }
        UUID playerUuid = profile.getId();
        if (playerUuid == null) {
            return;
        }
        DrunksManager drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        DrunkState drunkState = drunksManager.getDrunkState(playerUuid);
        String playerName = profile.getName();
        if (drunksManager.isPassedOut(playerUuid)) {
            String kickEventMessage = Config.config().events().kickEvent().kickEventMessage();
            event.kickMessage(
                    MiniMessage.miniMessage().deserialize(kickEventMessage == null ? TranslationsConfig.KICK_EVENT_MESSAGE : kickEventMessage,
                            MessageUtil.getDrunkStateTagResolver(drunkState), Placeholder.unparsed("player_name", playerName == null ? "" : playerName)
                    )
            );
            return;
        }
        if (Config.config().events().drunkenJoinDeny() && drunkState != null && drunkState.alcohol() >= 85 && RANDOM.nextInt(15) <= drunkState.alcohol() - 85) {
            event.kickMessage(
                    MiniMessage.miniMessage().deserialize(TranslationsConfig.DRUNKEN_JOIN_DENY_MESSAGE,
                            MessageUtil.getDrunkStateTagResolver(drunkState), Placeholder.unparsed("player_name", playerName == null ? "" : playerName)
                    )
            );
        }
    }
}
