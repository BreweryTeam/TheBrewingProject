package dev.jsinco.brewery.bukkit.migration;

import dev.jsinco.brewery.api.recipe.QualityData;
import dev.jsinco.brewery.api.util.Logger;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import javax.crypto.KeyGenerator;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

public class Migrations {
    private static final Pattern SIMPLE_NUMBER_PATTERN = Pattern.compile("(-|)\\d+(\\.\\d+|)");

    public static void migrateAllConfigFiles(File pluginFolder) {
        updateRecipes(new File(pluginFolder, "recipes.yml"));
        updateIncompleteRecipes(pluginFolder);
        migrateEncryptionKey(pluginFolder);
    }

    private static void updateIncompleteRecipes(File pluginFolder) {
        File recipesFile = new File(pluginFolder, "recipes.yml");
        File incompleteRecipes = new File(pluginFolder, "incomplete-recipes.yml");
        if (!recipesFile.exists() || !incompleteRecipes.exists()) {
            return;
        }
        YamlFile recipesYaml = new YamlFile(recipesFile);
        YamlFile incompleteYaml = new YamlFile(incompleteRecipes);
        try {
            recipesYaml.load();
            incompleteYaml.load();
        } catch (IOException e) {
            Logger.logErr("Unable to read recipes.yml file even though it existed");
            Logger.logErr(e);
            return;
        }
        if (!recipesYaml.isConfigurationSection("default-recipes")) {
            return;
        }
        ConfigurationSection defaultRecipesSection = recipesYaml.getConfigurationSection("default-recipes");
        ConfigurationSection incompleteRecipesSection = incompleteYaml.getConfigurationSection("incomplete-recipes");
        defaultRecipesSection.getKeys(true)
                .forEach(key -> incompleteRecipesSection.set(key, defaultRecipesSection.get(key)));
        recipesYaml.remove("default-recipes");
        try {
            incompleteYaml.save();
            recipesYaml.save();
        } catch (IOException e) {
            Logger.logErr("Unable to save recipes.yml or incomplete-recipes.yml.");
            Logger.logErr(e);
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

    /**
     * Detects old DES encryption keys, moves them to previousEncryptionKeys and sets
     * a valid AES-256 encryption key and the current one to be used for new potions
     */
    private static void migrateEncryptionKey(File pluginFolder) {
        File configFile = new File(pluginFolder, "config.yml");
        if (!configFile.exists()) return;

        YamlFile yaml = new YamlFile(configFile);
        try {
            yaml.load();
        } catch (IOException e) {
            Logger.logErr("Unable to read config.yml");
            Logger.logErr(e);
            return;
        }

        String currentKey = yaml.getString("encryptionKey");
        List<String> prevTokens = yaml.isList("previousEncryptionKeys")
                ? new ArrayList<>(yaml.getStringList("previousEncryptionKeys"))
                : new ArrayList<>();

        ParsedKey current = parseKey(currentKey);

        boolean currentIsAcceptableAes =
                current.decodable && (current.rawLength == 16 || current.rawLength == 24 || current.rawLength == 32);

        if (!currentIsAcceptableAes) {
            if (currentKey != null && !currentKey.isEmpty() && !prevTokens.contains(currentKey)) {
                prevTokens.add(currentKey);
            }

            String newAesToken = generateAesTokenPrefer256();
            yaml.set("encryptionKey", newAesToken);
            yaml.set("previousEncryptionKeys", prevTokens);

            try {
                yaml.save();
            } catch (IOException e) {
                Logger.logErr("Unable to save config.yml file");
                Logger.logErr(e);
            }
        }
    }

    private static final class ParsedKey {
        final boolean decodable;
        final int rawLength;
        ParsedKey(boolean decodable, int rawLength) { this.decodable = decodable; this.rawLength = rawLength; }
    }

    private static ParsedKey parseKey(String token) {
        if (token == null || token.isEmpty()) return new ParsedKey(false, 0);
        String b64 = token.trim();
        try {
            byte[] raw = Base64.getDecoder().decode(b64);
            return new ParsedKey(true, raw.length);
        } catch (IllegalArgumentException e) {
            return new ParsedKey(false, 0);
        }
    }

    private static String generateAesTokenPrefer256() {
        byte[] raw;
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            try {
                kg.init(256, SecureRandom.getInstanceStrong());
            } catch (Exception strongUnavailable) {
                kg.init(256, new SecureRandom());
            }
            raw = kg.generateKey().getEncoded();
        } catch (GeneralSecurityException e) {
            try {
                KeyGenerator kg = KeyGenerator.getInstance("AES");
                kg.init(128, new SecureRandom());
                raw = kg.generateKey().getEncoded();
            } catch (GeneralSecurityException ex) {
                throw new RuntimeException("Unable to generate AES key", ex);
            }
        }
        return Base64.getEncoder().encodeToString(raw);
    }

}
