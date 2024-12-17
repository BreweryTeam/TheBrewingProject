package dev.jsinco.brewery.paper.config.sections;

import dev.jsinco.brewery.api.config.Percentage;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@SuppressWarnings({"FieldMayBeFinal", "InnerClassMayBeStatic", "unused"})
public class RecipesConfig extends OkaeriConfig {
    private Map<String, ConfigurationRecipe> recipes = Map.of(
            "example",
            new ConfigurationRecipe()
                    .setBrewTime(2)
                    .setAlcohol(new Percentage(9))
                    .setIngredients(List.of("minecraft:diamond", "minecraft:bedrock/2", "nova|machines:iron_dust"))
                    .setPotionAttributes(new ConfigurationRecipe.PotionAttributes().setName("Example Brew"))
    );

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ConfigurationRecipe extends OkaeriConfig {
        @CustomKey("brew-time")
        private Integer brewTime;

        @CustomKey("brew-difficulty")
        private Integer brewDifficulty = 1;

        private Percentage alcohol = new Percentage(0);

        @CustomKey("cauldron-type")
        private String cauldronType = "WATER";

        @CustomKey("heat-sources")
        private List<String> heatSources;

        private List<String> ingredients;

        @CustomKey("potion-attributes")
        private PotionAttributes potionAttributes = new PotionAttributes();

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class PotionAttributes extends OkaeriConfig {
            private String name;

            private String color = "AQUA";

            private Boolean glint = false;

            @CustomKey("custom-model-data")
            private String customModelData = null;

            private List<String> lore = null;
        }

        private Distilling distilling = new Distilling();

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Distilling extends OkaeriConfig {
            private Integer runs = 0;

            private Integer time = 0;
        }

        private Aging aging = new Aging();

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Aging extends OkaeriConfig {
            @CustomKey("barrel-type")
            private String barrelType = null;

            private Integer years = 0;
        }

        private List<String> commands;

        private List<String> effects;

        private Map<String, String> messages;
    }

    private Map<String, ConfigurationDefaultRecipe> defaultRecipes;

    @Getter
    public static class ConfigurationDefaultRecipe extends OkaeriConfig {
        private String name = "Cauldron Brew";

        private List<String> lore;

        @CustomKey("custom-model-data")
        private String customModelData;

        private String color = "BLUE";

        private Boolean glint = false;
    }
}
