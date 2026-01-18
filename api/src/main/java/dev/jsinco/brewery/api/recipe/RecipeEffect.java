package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.moment.Interval;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public interface RecipeEffect {

    /**
     * The key of the base effect, which most probably is going to be in the minecraft namespace
     *
     * @return Key of the recipe effect
     */
    Key effectTypeKey();

    /**
     * @return A range of possible durations for this effect
     */
    Interval durationRange();

    /**
     * @return A range of possible amplifiers for this effect
     */
    Interval amplifierRange();

    /**
     * @return How the effect can be displayed
     */
    Component displayName();
}
