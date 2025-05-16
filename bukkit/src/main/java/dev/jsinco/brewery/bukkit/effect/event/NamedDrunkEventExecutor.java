package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.MessageUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.moment.Moment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class NamedDrunkEventExecutor {
    private static final Random RANDOM = new Random();
    private static final int STUMBLE_DURATION = 10;
    private static final int DRUNKEN_WALK_DURATION = 200;

    public static void doDrunkEvent(UUID playerUuid, NamedDrunkEvent event) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) {
            return;
        }
        switch (event) {
            case PUKE -> {
                PukeHandler pukeHandler = new PukeHandler(Config.PUKE_TIME, player);
                TheBrewingProject.getInstance().getActiveEventsRegistry().registerActiveEvent(playerUuid, event, Config.PUKE_TIME);
                Bukkit.getScheduler().runTaskTimer(TheBrewingProject.getInstance(), pukeHandler::tick, 0, 1);
            }
            case PASS_OUT -> {
                DrunksManagerImpl<?> drunksManager = TheBrewingProject.getInstance().getDrunksManager();
                player.kick(MessageUtil.compilePlayerMessage(Config.KICK_EVENT_MESSAGE == null ? TranslationsConfig.KICK_EVENT_MESSAGE : Config.KICK_EVENT_MESSAGE, player, drunksManager, 0));
                if (Config.KICK_EVENT_SERVER_MESSAGE != null) {
                    Component message = MessageUtil.compilePlayerMessage(Config.KICK_EVENT_SERVER_MESSAGE, player, drunksManager, 0);
                    Bukkit.getOnlinePlayers().forEach(player1 -> player1.sendMessage(message));
                }
                drunksManager.registerPassedOut(player.getUniqueId());
            }
            case STUMBLE -> {
                DrunksManagerImpl<?> drunksManager = TheBrewingProject.getInstance().getDrunksManager();
                int duration = RANDOM.nextInt(STUMBLE_DURATION / 2, STUMBLE_DURATION * 3 / 2 + 1);
                StumbleHandler stumbleHandler = new StumbleHandler(duration, player, drunksManager);
                TheBrewingProject.getInstance().getActiveEventsRegistry().registerActiveEvent(playerUuid, event, duration);
                Bukkit.getScheduler().runTaskTimer(TheBrewingProject.getInstance(), stumbleHandler::doStumble, 0, 1);
            }
            case CHICKEN -> {
                player.getWorld().spawn(player.getLocation(), Chicken.class, chicken -> {
                    chicken.setEggLayTime(Integer.MAX_VALUE);
                    chicken.setPersistent(false);
                    chicken.setAge(0);
                    chicken.setLootTable(new LootTable() {
                        @Override
                        public @NotNull Collection<ItemStack> populateLoot(@Nullable Random random, @NotNull LootContext context) {
                            return List.of();
                        }

                        @Override
                        public void fillInventory(@NotNull Inventory inventory, @Nullable Random random, @NotNull LootContext context) {

                        }

                        @Override
                        public @NotNull NamespacedKey getKey() {
                            return NamespacedKey.fromString("brewery:empty");
                        }
                    });
                    chicken.setBreed(false);
                });
                player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.CHICKEN_MESSAGE, MessageUtil.getPlayerTagResolver(player)));
            }
            case DRUNK_MESSAGE -> {
                List<String> drunkMessages = Config.DRUNK_MESSAGES;
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
                String teleport = Config.TELEPORT_DESTINATIONS.get(RANDOM.nextInt(Config.TELEPORT_DESTINATIONS.size()));
                Location location = BukkitAdapter.parseLocation(teleport);
                if (location == null) {
                    return;
                }
                player.teleport(location);
                player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.TELEPORT_MESSAGE, MessageUtil.getPlayerTagResolver(player)));
            }
            case NAUSEA -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, RANDOM.nextInt(Moment.MINUTE / 2, Moment.MINUTE * 3 / 2), 1));
            }
            case DRUNKEN_WALK -> {
                int duration = RANDOM.nextInt(DRUNKEN_WALK_DURATION / 2, DRUNKEN_WALK_DURATION * 3 / 2);
                DrunkenWalkHandler drunkenWalkHandler = new DrunkenWalkHandler(duration, player, TheBrewingProject.getInstance().getDrunksManager());
                Bukkit.getScheduler().runTaskTimer(TheBrewingProject.getInstance(), drunkenWalkHandler::tick, 0, 1);
                TheBrewingProject.getInstance().getActiveEventsRegistry().registerActiveEvent(playerUuid, event, duration);
            }
        }
    }
}
