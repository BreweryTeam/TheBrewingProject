package dev.jsinco.brewery.paper.config;

import dev.jsinco.brewery.paper.TheBrewingProject;
import dev.jsinco.brewery.paper.config.sections.Config;
import dev.jsinco.brewery.paper.config.sections.CustomIngredientsConfig;
import dev.jsinco.brewery.paper.config.sections.RecipesConfig;
import dev.jsinco.brewery.paper.config.serdes.PercentageTransformer;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

@Getter
public class ConfigManager {
    private final Config config;
    private final RecipesConfig recipes;
    private final CustomIngredientsConfig customIngredients;

    public ConfigManager() {
        config = createConfig(Config.class, "config.yml");
        recipes = createConfig(RecipesConfig.class, "recipes.yml");
        customIngredients = createConfig(CustomIngredientsConfig.class, "custom-ingredients.yml");
    }

    private static Yaml createYaml() {
        LoaderOptions loaderOptions = new LoaderOptions();
        Constructor constructor = new Constructor(loaderOptions);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setIndent(4);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Representer representer = new Representer(dumperOptions);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Resolver resolver = new Resolver();

        return new Yaml(constructor, representer, dumperOptions, loaderOptions, resolver);
    }

    private <T extends OkaeriConfig> T createConfig(Class<T> configClass, String filename) {
        return eu.okaeri.configs.ConfigManager.create(configClass, (it) -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer(createYaml()), new StandardSerdes());
            it.withBindFile(TheBrewingProject.getInstance().getDataFolder().toPath().resolve(filename));
            it.withRemoveOrphans(true);
            it.withSerdesPack(registry -> {
                registry.register(new PercentageTransformer());
            });
            it.saveDefaults();
            it.load(true);
        });
    }
}
