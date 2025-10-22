package dev.jsinco.brewery.configuration;


import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientGroup;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.ingredient.ScoredIngredient;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.util.FutureUtil;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Pattern;

@Accessors(fluent = true)
@Getter
public class IngredientsSection extends OkaeriConfig {

    @CustomKey("ingredient-groups")
    @Comment({
            "To reference a ingredient group in your recipe, use the key",
            "'#brewery:my_ingredient_group_key' normally as any ingredient.",
            "You can also define vanilla tags here, for example #leaves"
    })
    private List<IngredientGroupSection> ingredientGroups = List.of(
            new IngredientGroupSection("grass", Component.text("Grass"), List.of(
                    "+grass_block",
                    "++fern",
                    "+++short_grass"
            ))
    );

    @Exclude
    private static IngredientsSection instance;
    @Exclude
    private static CompletableFuture<Void> validatedFuture;

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

    public static void validate(IngredientManager<?> ingredientManager) {
        Set<String> keys = new HashSet<>();
        List<CompletableFuture<Optional<IngredientGroupSection>>> updatedSections = new ArrayList<>();
        for (IngredientGroupSection ingredientGroup : instance.ingredientGroups()) {
            String key = ingredientGroup.key;
            Preconditions.checkArgument(!keys.contains(key), "Can't have two ingredient groups with the same key (ingredients.yml): " + key);
            keys.add(key);
            List<CompletableFuture<Optional<Ingredient>>> ingredientsFutures = ingredientGroup.materials.stream()
                    .map(string -> IngredientGroupSection.INGREDIENT_GROUP_PATTERN.matcher(string).replaceAll(""))
                    .map(ingredientManager::getIngredient)
                    .toList();
            FutureUtil.mergeFutures(ingredientsFutures)
                    .thenApplyAsync(ingredients -> {
                        if (ingredients.stream().allMatch(Optional::isPresent)) {
                            return Optional.of(ingredientGroup);
                        } else {
                            Logger.logErr("Could not resolve ingredient group '" + key + "', undefined material");
                            return Optional.empty();
                        }
                    });
        }
        IngredientsSection.validatedFuture = FutureUtil.mergeFutures(updatedSections).thenAccept(newGroups -> {
            ingredients().ingredientGroups.clear();
            ingredients().ingredientGroups.addAll(newGroups.stream().flatMap(Optional::stream).toList());
        });
    }


    @Accessors(fluent = true)
    public static class IngredientGroupSection extends OkaeriConfig {
        @Getter
        private String key;
        @CustomKey("display-name")
        private Component displayName;
        private List<String> materials;

        @Exclude
        private static final Pattern INGREDIENT_GROUP_PATTERN = Pattern.compile("^\\+{0,3}#brewery:");

        public IngredientGroupSection() {
            // NO-OP, keep for deserialization
        }

        public IngredientGroupSection(String key, Component displayName, List<String> materials) {
            this.key = key;
            this.displayName = displayName;
            this.materials = materials;
        }

        public CompletableFuture<Optional<Ingredient>> create(IngredientManager<?> ingredientManager, Function<String, List<String>> tagResolver) {
            List<CompletableFuture<Optional<Ingredient>>> group = new ArrayList<>();
            for (String material : materials) {
                if (INGREDIENT_GROUP_PATTERN.matcher(material).find()) {
                    Logger.logErr("Ingredient groups are not allowed to reference other groups!");
                    return CompletableFuture.completedFuture(Optional.empty());
                }
                List<String> strings;
                String withoutScores = material.replaceAll("^\\+{1,3}", "");
                if (withoutScores.startsWith("#")) {
                    strings = tagResolver.apply(withoutScores.replaceFirst("#", ""));
                    if (strings == null) {
                        Logger.logErr("Invalid item tag: " + withoutScores);
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                } else {
                    strings = List.of(withoutScores);
                }
                strings.forEach(tagMaterial -> group.add(ingredientManager.getIngredient(tagMaterial)
                        .thenApplyAsync(ingredient -> {
                                    Optional<Ingredient> ingredientOptional = ingredient.map(
                                            ingredient0 -> parseScore(ingredient0, material)
                                    );
                                    if (ingredientOptional.isEmpty()) {
                                        Logger.logErr("Unknown ingredient: " + tagMaterial);
                                    }
                                    return ingredientOptional;
                                }
                        )));
            }
            return validatedFuture.thenCombine(FutureUtil.mergeFutures(group)
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
                    }), (voidObj, ingredientGroup) -> ingredientGroup.map(Ingredient.class::cast));
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
