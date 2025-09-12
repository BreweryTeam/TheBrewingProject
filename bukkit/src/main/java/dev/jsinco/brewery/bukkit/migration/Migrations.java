package dev.jsinco.brewery.bukkit.migration;

import dev.jsinco.brewery.api.recipe.QualityData;
import dev.jsinco.brewery.api.util.Logger;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class Migrations {
    private static final Pattern SIMPLE_NUMBER_PATTERN = Pattern.compile("(-|)\\d+(\\.\\d+|)");

    public static void migrateAllConfigFiles(File pluginFolder) {
        updateRecipes(new File(pluginFolder, "recipes.yml"));
        updateEvents(new File(pluginFolder, "events.yml"));
    }

    private static void updateEvents(File eventsFile) {
        if (!eventsFile.exists()) {
            return;
        }
        YamlFile yaml = new YamlFile(eventsFile);
        try {
            yaml.load();
        } catch (IOException e) {
            Logger.logErr("Unable to read events.yml file even though it existed");
            Logger.logErr(e);
            return;
        }
    }

    private static void updateRecipes(File recipesFile) {
        if (!recipesFile.exists()) {
            return;
        }
        YamlFile yaml = new YamlFile(recipesFile);
        try {
            yaml.load();
        } catch (IOException e) {
            Logger.logErr("Unable to read recipes.yml file even though it existed");
            Logger.logErr(e);
            return;
        }
        ConfigurationSection section = yaml.getConfigurationSection("recipes");
        for (String recipeKey : section.getKeys(false)) {
            ConfigurationSection recipe = section.getConfigurationSection(recipeKey);
            for (String key : recipe.getKeys(false)) {
                if (key.equals("alcohol")) {
                    ConfigurationSection modifiers = recipe.createSection("modifiers");
                    String alcoholString = recipe.getString("alcohol");
                    String toxinsString = QualityData.toQualityFactoredString(
                            QualityData.readQualityFactoredString(alcoholString)
                                    .map(string -> string.replace("%", ""))
                                    .map(Double::parseDouble)
                                    .qualityMap((quality, aDouble) -> switch (quality) {
                                        case BAD -> aDouble > 0 ? aDouble * 2 / 3 : aDouble * 1 / 4;
                                        case GOOD -> aDouble * 1 / 2;
                                        case EXCELLENT -> aDouble > 0 ? aDouble * 1 / 4 : aDouble * 2 / 3;
                                    })
                                    .map(String::valueOf)
                    );
                    if (SIMPLE_NUMBER_PATTERN.matcher(alcoholString).matches()) {
                        modifiers.set("alcohol", Double.parseDouble(alcoholString));
                    } else {
                        modifiers.set("alcohol", alcoholString);
                    }
                    recipe.remove("alcohol");
                    modifiers.set("toxins", toxinsString);
                }
            }
        }
        try {
            yaml.save();
        } catch (IOException e) {
            Logger.logErr("Unable to save recipes.yml file");
            Logger.logErr(e);
        }
    }
}
