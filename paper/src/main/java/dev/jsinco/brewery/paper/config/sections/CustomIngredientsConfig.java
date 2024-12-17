package dev.jsinco.brewery.paper.config.sections;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CustomIngredientsConfig extends OkaeriConfig {
    @CustomKey("custom-ingredients")
    private Map<String, CustomIngredient> customIngredients = new HashMap<>();

    @Getter
    public class CustomIngredient extends OkaeriConfig {
        private String name;

        private String material;

        @CustomKey("custom-model-data")
        private String modelData;

        private List<String> lore;
    }
}
