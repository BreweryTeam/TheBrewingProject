package dev.jsinco.brewery.configuration;


import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientGroup;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.ingredient.ScoredIngredient;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.util.FutureUtil;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Accessors(fluent = true)
@Getter
public class IngredientsSection extends OkaeriConfig {

    @CustomKey("ingredient-groups")
    private List<CustomIngredientSection> customIngredients = List.of(
            new CustomIngredientSection("grass", Component.text("Grass"), List.of(
                    "+grass_block",
                    "++fern",
                    "+++short_grass"
            ))
    );

    @Exclude
    private static IngredientsSection instance;

    public static IngredientsSection ingredients() {
        return instance;
    }

    public static void load(File dataFolder, OkaeriSerdesPack... packs) {
        IngredientsSection.instance = ConfigManager.create(IngredientsSection.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer(), packs);
            it.withBindFile(new File(dataFolder, "ingredients.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }


    @Accessors(fluent = true)
    public static class CustomIngredientSection extends OkaeriConfig {
        @Getter
        private String key;
        @CustomKey("display-name")
        private Component displayName;
        private List<String> materials;

        private static final Pattern INGREDIENT_GROUP_PATTERN = Pattern.compile("^\\+{0,3}#brewery:");

        public CustomIngredientSection() {
            // NO-OP, keep for deserialization
        }

        public CustomIngredientSection(String key, Component displayName, List<String> materials) {
            this.key = key;
            this.displayName = displayName;
            this.materials = materials;
        }

        public CompletableFuture<Optional<Ingredient>> create(IngredientManager<?> ingredientManager) {
            List<CompletableFuture<Optional<Ingredient>>> group = new ArrayList<>();
            for (String material : materials) {
                if (INGREDIENT_GROUP_PATTERN.matcher(material).find()) {
                    Logger.logErr("Ingredient groups are not allowed to reference other groups!");
                    return CompletableFuture.completedFuture(Optional.empty());
                }
                group.add(
                        ingredientManager.getIngredient(material.replaceAll("^\\+{1,3}", ""))
                                .thenApplyAsync(ingredient -> {
                                            Optional<Ingredient> ingredientOptional = ingredient.map(
                                                    ingredient0 -> parseScore(ingredient0, material)
                                            );
                                            if (ingredientOptional.isEmpty()) {
                                                Logger.logErr("Unknown ingredient: " + material);
                                            }
                                            return ingredientOptional;
                                        }
                                ));
            }
            return FutureUtil.mergeFutures(group)
                    .thenApplyAsync(ingredients -> {
                        if (ingredients.stream().anyMatch(Optional::isEmpty)) {
                            return Optional.empty();
                        }
                        return Optional.of(new IngredientGroup(
                                "#brewery:" + key,
                                displayName,
                                ingredients.stream()
                                        .map(Optional::get)
                                        .toList()
                        ));
                    });
        }

        private Ingredient parseScore(Ingredient ingredient, String ingredientString) {
            if (ingredientString.startsWith("+++")) {
                return new ScoredIngredient(ingredient, 1.0);
            }
            if (ingredientString.startsWith("++")) {
                return new ScoredIngredient(ingredient, 0.7);
            }
            if (ingredientString.startsWith("+")) {
                return new ScoredIngredient(ingredient, 0.3);
            }
            return ingredient;
        }
    }
}
