package dev.jsinco.brewery.bukkit.api;

import dev.jsinco.brewery.api.brew.BrewManager;
import dev.jsinco.brewery.api.integration.IntegrationManager;
import dev.jsinco.brewery.api.effect.DrunksManager;
import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import dev.jsinco.brewery.api.structure.PlacedStructureRegistry;
import org.bukkit.inventory.ItemStack;

public interface TheBrewingProjectApi {

    /**
     * @return A brew manager instance that helps you create and read brews
     */
    BrewManager<ItemStack> getBrewManager();

    /**
     * @return A drunks manager that helps you manage player drunkeness and plan events
     */
    DrunksManager getDrunksManager();

    /**
     * @return A registry of every recipe
     */
    RecipeRegistry<ItemStack> getRecipeRegistry();

    /**
     * @return A registry of every structure placed in the world
     */
    PlacedStructureRegistry getPlacedStructureRegistry();

    /**
     * @return An integration manager, that allows you to register integrations
     */
    IntegrationManager getIntegrationManager();
}
