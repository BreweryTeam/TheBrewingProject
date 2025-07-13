package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.vector.BreweryLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

public class NamedDrunkEventExecutor {
    private static final Random RANDOM = new Random();
    private static final int STUMBLE_DURATION = 10;
    private static final int DRUNKEN_WALK_DURATION = 400;
    public static final NamespacedKey NO_DROPS = new NamespacedKey("brewery", "no_drops");

    public static void doDrunkEvent(UUID playerUuid, NamedDrunkEvent event) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) {
            return;
        }
        switch (event) {
            case PUKE -> {
                PukeHandler pukeHandler = new PukeHandler(Config.config().puke().pukeTime(), player);
                TheBrewingProject.getInstance().getActiveEventsRegistry().registerActiveEvent(playerUuid, event, Config.config().puke().pukeTime());
                Bukkit.getScheduler().runTaskTimer(TheBrewingProject.getInstance(), pukeHandler::tick, 0, 1);
            }
            case PASS_OUT -> {
                DrunksManagerImpl<?> drunksManager = TheBrewingProject.getInstance().getDrunksManager();
                EventSection.KickEventSection kickEventSection = Config.config().events().kickEvent();
                player.kick(BukkitMessageUtil.compilePlayerMessage(kickEventSection.kickEventMessage() == null ? TranslationsConfig.KICK_EVENT_MESSAGE : kickEventSection.kickEventMessage(), player, drunksManager, 0));
                if (kickEventSection.kickServerMessage() != null) {
                    Component message = BukkitMessageUtil.compilePlayerMessage(kickEventSection.kickServerMessage(), player, drunksManager, 0);
                    Bukkit.getOnlinePlayers().forEach(player1 -> player1.sendMessage(message));
                }
                drunksManager.registerPassedOut(player.getUniqueId());
            }
            case STUMBLE -> {
                int duration = RANDOM.nextInt(STUMBLE_DURATION / 2, STUMBLE_DURATION * 3 / 2 + 1);
                StumbleHandler stumbleHandler = new StumbleHandler(duration, player);
                TheBrewingProject.getInstance().getActiveEventsRegistry().registerActiveEvent(playerUuid, event, duration);
                Bukkit.getScheduler().runTaskTimer(TheBrewingProject.getInstance(), stumbleHandler::doStumble, 0, 1);
            }
            case CHICKEN -> {
                player.getWorld().spawn(player.getLocation(), Chicken.class, chicken -> {
                    chicken.setEggLayTime(Integer.MAX_VALUE);
                    chicken.setPersistent(false);
                    chicken.setAge(0);
                    chicken.getPersistentDataContainer().set(NO_DROPS, PersistentDataType.BOOLEAN, true);
                    chicken.setBreed(false);
                });
                player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.CHICKEN_MESSAGE, BukkitMessageUtil.getPlayerTagResolver(player)));
            }
            case DRUNK_MESSAGE -> {
                List<String> drunkMessages = Config.config().events().drunkMessages();
                if (drunkMessages.isEmpty()) {
                    return;
                }
                List<Player> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                        .filter(Player::isVisibleByDefault)
                        .filter(player1 -> !player.equals(player1))
                        .map(Player.class::cast)
                        .toList();
                if (onlinePlayers.isEmpty()) {
                    return;
                }
                Player randomPlayer = onlinePlayers.get(RANDOM.nextInt(onlinePlayers.size()));
                player.chat(drunkMessages.get(RANDOM.nextInt(drunkMessages.size())).replace("<random_player_name>", randomPlayer.getName()));
            }
            case TELEPORT -> {
                List<BreweryLocation> locations = Config.config().events().teleportDestinations().stream().map(Supplier::get).toList();
                if (locations.isEmpty()) {
                    return;
                }
                BreweryLocation teleport = locations.get(RANDOM.nextInt(locations.size()));
                Location location = BukkitAdapter.toLocation(teleport);
                player.teleport(location);
                player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.TELEPORT_MESSAGE, BukkitMessageUtil.getPlayerTagResolver(player)));
            }
            case NAUSEA -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, RANDOM.nextInt(Moment.MINUTE / 2, Moment.MINUTE * 3 / 2), 1));
            }
            case DRUNKEN_WALK -> {
                int duration = RANDOM.nextInt(DRUNKEN_WALK_DURATION / 2, DRUNKEN_WALK_DURATION * 3 / 2);
                DrunkenWalkHandler drunkenWalkHandler = new DrunkenWalkHandler(duration, player);
                Bukkit.getScheduler().runTaskTimer(TheBrewingProject.getInstance(), drunkenWalkHandler::tick, 0, 1);
                TheBrewingProject.getInstance().getActiveEventsRegistry().registerActiveEvent(playerUuid, event, duration);
            }
        }
    }
}
