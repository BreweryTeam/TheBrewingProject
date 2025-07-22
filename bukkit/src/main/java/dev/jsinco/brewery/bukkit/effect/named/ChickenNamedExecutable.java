package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.ExecutableEventStep;
import dev.jsinco.brewery.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class ChickenNamedExecutable implements ExecutableEventStep {

    public static final NamespacedKey NO_DROPS = new NamespacedKey("brewery", "no_drops");

    @Override
    public void execute(UUID contextPlayer, List<EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return;
        }

        player.getWorld().spawn(player.getLocation(), Chicken.class, chicken -> {
            chicken.setEggLayTime(Integer.MAX_VALUE);
            chicken.setPersistent(false);
            chicken.setAge(0);
            chicken.getPersistentDataContainer().set(NO_DROPS, PersistentDataType.BOOLEAN, true);
            chicken.setBreed(false);
        });
        MessageUtil.msg(player, TranslationsConfig.CHICKEN_MESSAGE, BukkitMessageUtil.getPlayerTagResolver(player));
    }

}
