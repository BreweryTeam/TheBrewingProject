package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.recipe.RecipeAttributeModifierImpl;
import dev.jsinco.brewery.api.util.Logger;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BrewAttributeModifierManager implements Listener {

    private final Map<UUID, Map<ModifierId, ActiveModifier>> activeModifiers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> tickTasks = new ConcurrentHashMap<>();

    public void apply(Player player, List<RecipeAttributeModifierImpl> modifiers) {
        if (modifiers.isEmpty()) {
            return;
        }
        if (!Bukkit.isOwnedByCurrentRegion(player)) {
            player.getScheduler().run(TheBrewingProject.getInstance(), ignored -> apply(player, modifiers), null);
            return;
        }
        Map<ModifierId, ActiveModifier> playerModifiers = activeModifiers.computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>());
        for (RecipeAttributeModifierImpl recipeModifier : modifiers) {
            AttributeInstance instance = player.getAttribute(recipeModifier.attribute());
            if (instance == null) {
                continue;
            }
            long duration = recipeModifier.randomDuration();
            if (duration <= 0) {
                continue;
            }
            NamespacedKey key = recipeModifier.key();
            AttributeModifier attributeModifier = new AttributeModifier(key, recipeModifier.randomAmount(), recipeModifier.operation());
            instance.removeModifier(key);
            instance.addTransientModifier(attributeModifier);
            playerModifiers.put(new ModifierId(recipeModifier.attribute(), key), new ActiveModifier(recipeModifier.attribute(), attributeModifier, duration));
        }
        if (playerModifiers.isEmpty()) {
            activeModifiers.remove(player.getUniqueId());
            return;
        }
        ensureTicking(player);
    }

    public void clear(Player player) {
        ScheduledTask task = tickTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        Map<ModifierId, ActiveModifier> playerModifiers = activeModifiers.remove(player.getUniqueId());
        if (playerModifiers == null) {
            return;
        }
        playerModifiers.values().forEach(activeModifier -> activeModifier.removeFrom(player));
    }

    public void clearAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                clear(player);
            } catch (Throwable e) {
                Logger.logErr(e);
            }
        }
        activeModifiers.clear();
        tickTasks.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Map<ModifierId, ActiveModifier> playerModifiers = activeModifiers.get(player.getUniqueId());
        if (playerModifiers == null || playerModifiers.isEmpty()) {
            return;
        }
        playerModifiers.values().forEach(activeModifier -> activeModifier.addTo(player));
        ensureTicking(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        clear(event.getPlayer());
    }

    private void ensureTicking(Player player) {
        UUID uuid = player.getUniqueId();
        ScheduledTask existing = tickTasks.get(uuid);
        if (existing != null && !existing.isCancelled()) {
            return;
        }
        ScheduledTask task = player.getScheduler().runAtFixedRate(
                TheBrewingProject.getInstance(),
                scheduledTask -> tick(player, scheduledTask),
                () -> tickTasks.remove(uuid),
                1, 1
        );
        if (task != null) {
            tickTasks.put(uuid, task);
        }
    }

    private void tick(Player player, ScheduledTask task) {
        UUID uuid = player.getUniqueId();
        Map<ModifierId, ActiveModifier> playerModifiers = activeModifiers.get(uuid);
        if (playerModifiers != null) {
            Iterator<ActiveModifier> iterator = playerModifiers.values().iterator();
            while (iterator.hasNext()) {
                ActiveModifier activeModifier = iterator.next();
                if (--activeModifier.remainingTicks <= 0) {
                    activeModifier.removeFrom(player);
                    iterator.remove();
                }
            }
        }
        if (playerModifiers == null || playerModifiers.isEmpty()) {
            activeModifiers.remove(uuid, playerModifiers);
            task.cancel();
            tickTasks.remove(uuid, task);
        }
    }

    private record ModifierId(Attribute attribute, NamespacedKey key) {
    }

    private static final class ActiveModifier {
        private final Attribute attribute;
        private final AttributeModifier modifier;
        private long remainingTicks;

        private ActiveModifier(Attribute attribute, AttributeModifier modifier, long remainingTicks) {
            this.attribute = attribute;
            this.modifier = modifier;
            this.remainingTicks = remainingTicks;
        }

        private void addTo(Player player) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                return;
            }
            instance.removeModifier(modifier.getKey());
            instance.addTransientModifier(modifier);
        }

        private void removeFrom(Player player) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(modifier.getKey());
            }
        }
    }
}
