package dev.jsinco.brewery.bukkit.animation;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.BukkitIngredientUtil;
import dev.jsinco.brewery.configuration.AnimationDisplay;
import dev.jsinco.brewery.configuration.Config;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public class AnimationManager {

    private static final Random RANDOM = new Random();
    private static final int BOTTLE_EMPTY_DURATION = 40;

    private AnimationManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     *
     * @return The animation duration
     */
    public static long playIngredientAddAnimation(ItemStack ingredient, Player player, Location toLocation) {
        Optional<ItemStack> ingredientTransform = BukkitIngredientUtil.computeTransform(ingredient);
        if (ingredientTransform.isPresent()) {
            return ingredientEmptyAnimation(ingredient, player, toLocation, ingredientTransform.get());
        }
        return ingredientThrowAnimation(ingredient, player, toLocation);
    }


    private static long ingredientThrowAnimation(ItemStack ingredient, Player player, Location toLocation) {
        Function<Location, ItemDisplay> itemDisplay = (location) -> player.getWorld().spawn(location, ItemDisplay.class, entity -> {
            entity.setPersistent(false);
            entity.setItemStack(ingredient);
            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
            entity.setTeleportDuration(1);
            if (Config.config().cauldrons().ingredientAddedAnimation() == AnimationDisplay.BREWER) {
                entity.setVisibleByDefault(false);
                player.showEntity(TheBrewingProject.getInstance(), entity);
            }
        });
        IngredientAddAnimation ingredientAddAnimation = new IngredientAddAnimation(player.getLocation(), toLocation, itemDisplay);
        player.getScheduler().runAtFixedRate(
                TheBrewingProject.getInstance(),
                ingredientAddAnimation,
                ingredientAddAnimation::close,
                1,
                1
        );
        return IngredientAddAnimation.T_END;
    }

    private static long ingredientEmptyAnimation(ItemStack ingredient, Player player, Location toLocation, ItemStack transformed) {
        float yawRadians = (float) ((player.getYaw() - 90) / 360 * Math.PI * 2);
        Vector3f translation = new Vector3f(
                (float) Math.sin(yawRadians) * 0.2f,
                0,
                (float) -Math.cos(yawRadians) * 0.2f
        );
        Quaternionf towardsPlayer = new AxisAngle4f((float) Math.PI * 3 / 2 - yawRadians, 0, 1, 0)
                .get(new Quaternionf());
        ItemDisplay itemDisplay = player.getWorld().spawn(
                toLocation.clone().add(RANDOM.nextDouble(-0.25, 0.25), 0.75, RANDOM.nextDouble(-0.25, 0.25)),
                ItemDisplay.class,
                entity -> {
                    entity.setPersistent(false);
                    entity.setItemStack(ingredient);
                    entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
                    entity.setTransformation(new Transformation(
                            translation,
                            towardsPlayer,
                            new Vector3f(1, 1, 1),
                            new Quaternionf()
                    ));
                    entity.setInterpolationDuration(BOTTLE_EMPTY_DURATION / 4);
                    if (Config.config().cauldrons().ingredientAddedAnimation() == AnimationDisplay.BREWER) {
                        entity.setVisibleByDefault(false);
                    }
                });
        if (Config.config().cauldrons().ingredientAddedAnimation() == AnimationDisplay.BREWER) {
            player.showEntity(TheBrewingProject.getInstance(), itemDisplay);
        }
        Quaternionf tilting = new AxisAngle4f(
                (float) Math.PI * 2 / 3,
                (float) Math.cos(yawRadians),
                0,
                (float) Math.sin(yawRadians)
        ).get(new Quaternionf());
        itemDisplay.getScheduler().runDelayed(
                TheBrewingProject.getInstance(),
                ignored -> {
                    itemDisplay.setInterpolationDelay(0);
                    itemDisplay.setTransformation(new Transformation(
                            translation,
                            tilting,
                            new Vector3f(1, 1, 1),
                            towardsPlayer
                    ));
                },
                itemDisplay::remove,
                2
        );
        itemDisplay.getScheduler().runDelayed(
                TheBrewingProject.getInstance(),
                ignored -> {
                    itemDisplay.setItemStack(transformed);
                    itemDisplay.setInterpolationDelay(0);
                    itemDisplay.setTransformation(new Transformation(
                            translation,
                            towardsPlayer,
                            new Vector3f(1, 1, 1),
                            new Quaternionf()
                    ));
                },
                itemDisplay::remove,
                BOTTLE_EMPTY_DURATION * 3 / 4
        );
        itemDisplay.getScheduler().runDelayed(
                TheBrewingProject.getInstance(),
                ignored -> itemDisplay.remove(),
                itemDisplay::remove,
                BOTTLE_EMPTY_DURATION
        );
        return BOTTLE_EMPTY_DURATION / 4;
    }
}
