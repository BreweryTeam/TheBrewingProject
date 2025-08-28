package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ChickenNamedExecutable implements EventPropertyExecutable {

    public static final NamespacedKey NO_DROPS = new NamespacedKey("brewery", "no_drops");

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        player.getWorld().spawn(player.getLocation(), Chicken.class, chicken -> {
            chicken.setEggLayTime(Integer.MAX_VALUE);
            chicken.setPersistent(false);
            chicken.setAge(0);
            chicken.getPersistentDataContainer().set(NO_DROPS, PersistentDataType.BOOLEAN, true);
            chicken.setBreed(false);
        });
        MessageUtil.message(player, "tbp.events.chicken-message", BukkitMessageUtil.getPlayerTagResolver(player));
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
