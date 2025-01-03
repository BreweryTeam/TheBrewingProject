package dev.jsinco.brewery;

import dev.jsinco.brewery.factories.RecipeFactory;
import dev.jsinco.brewery.listeners.BlockEventListener;
import dev.jsinco.brewery.recipes.ingredient.PluginIngredient;
import dev.jsinco.brewery.recipes.ingredient.external.OraxenPluginIngredient;
import dev.jsinco.brewery.structure.BreweryStructure;
import dev.jsinco.brewery.structure.StructureReader;
import dev.jsinco.brewery.structure.StructureRegistry;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

public class TheBrewingProject extends JavaPlugin {

    @Getter
    private static TheBrewingProject instance;
    @Getter
    private RecipeFactory recipeFactory;
    private StructureRegistry structureRegistry;

    @Override
    public void onLoad() {
        instance = this;
        this.recipeFactory = new RecipeFactory();
        this.structureRegistry = new StructureRegistry();

        Stream.of("/structures/small_barrel.json", "/structures/large_barrel.json")
                .map(string -> {
                    try{
                        return StructureReader.fromInternalResourceJson(string);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .forEach(structureRegistry::addStructures);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(this.structureRegistry), this);
    }


    public void registerPluginIngredients() {
        PluginIngredient.registerPluginIngredient("Oraxen", OraxenPluginIngredient::new);
    }
}