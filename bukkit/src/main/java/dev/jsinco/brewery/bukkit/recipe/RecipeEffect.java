package dev.jsinco.brewery.bukkit.recipe;

import dev.jsinco.brewery.api.moment.Interval;
import dev.jsinco.brewery.api.moment.Moment;
import net.kyori.adventure.text.Component;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public record RecipeEffect(PotionEffectType type, Interval durationRange, Interval amplifierRange) {

    private static final Random RANDOM = new Random();

    public PotionEffect newPotionEffect() {
        return new PotionEffect(type,
                RANDOM.nextInt((int) durationRange.start(), (int) (durationRange.stop() + 1)) * Moment.SECOND,
                RANDOM.nextInt((int) amplifierRange.start(), (int) (amplifierRange.stop() + 1))
        );
    }

    public Component displayName() {
        return Component.translatable(type().translationKey())
                .append(Component.text(" "))
                .append(amplifierRange().displayName())
                .append(Component.text(" "))
                .append(durationRange().displayName())
                .append(Component.text("s"));
    }
}
