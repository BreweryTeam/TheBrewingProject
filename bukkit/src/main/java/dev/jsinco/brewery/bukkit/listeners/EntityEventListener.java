package dev.jsinco.brewery.bukkit.listeners;

import dev.jsinco.brewery.bukkit.effect.named.ChickenNamedExecutable;
import dev.jsinco.brewery.bukkit.recipe.RecipeEffects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import java.util.Optional;

public class EntityEventListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(ChickenNamedExecutable.NO_DROPS)) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        Optional<RecipeEffects> recipeEffectsOptional = RecipeEffects.fromEntity(event.getEntity());
        recipeEffectsOptional.ifPresent(recipeEffects ->
                event.getAffectedEntities().stream()
                        .filter(Player.class::isInstance)
                        .map(Player.class::cast)
                        .forEach(recipeEffects::applyTo));
    }

}
