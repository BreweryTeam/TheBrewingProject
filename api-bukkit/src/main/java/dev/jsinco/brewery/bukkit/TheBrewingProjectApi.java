package dev.jsinco.brewery.bukkit;

import dev.jsinco.brewery.brew.BrewManager;
import dev.jsinco.brewery.integration.IntegrationManager;
import dev.jsinco.brewery.effect.DrunksManager;
import dev.jsinco.brewery.recipe.RecipeRegistry;
import dev.jsinco.brewery.structure.PlacedStructureRegistry;
import org.bukkit.inventory.ItemStack;

public interface TheBrewingProjectApi {


    BrewManager<ItemStack> getBrewManager();

    DrunksManager getDrunksManager();

    RecipeRegistry<ItemStack> getRecipeRegistry();

    PlacedStructureRegistry getPlacedStructureRegistry();

    IntegrationManager getIntegrationManager();
}
