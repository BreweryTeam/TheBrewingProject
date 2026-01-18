package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Holder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface RecipeEffects {

    /**
     * @return All potion effects this effect will apply
     */
    List<? extends RecipeEffect> effects();

    /**
     * Use {@link #formatMessage(String, Holder.Player)} to convert into component
     *
     * @return A mini message formatted title message
     */
    @Nullable String titleMessage();

    /**
     * Use {@link #formatMessage(String, Holder.Player)} to convert into component
     *
     * @return A mini message formatted chat message
     */
    @Nullable String chatMessage();

    /**
     * Use {@link #formatMessage(String, Holder.Player)} to convert into component
     *
     * @return A mini message formatted action bar message
     */
    @Nullable String actionBarMessage();

    /**
     *
     * @return A map with every changed modifier and it's change amount
     */
    Map<DrunkenModifier, Double> modifiersChange();

    /**
     * @return A list of all events that are going to be called
     */
    List<BreweryKey> events();

    /**
     * Utility function to format mini message strings, and insert tag resolvers. Used for {@link #titleMessage()},
     * {@link #chatMessage()} and {@link #actionBarMessage()}.
     *
     * @param message The mini message to format
     * @param player  The player to format this message for
     * @return A formatted message component
     */
    @Nullable Component formatMessage(String message, Holder.Player player);
}
