package dev.jsinco.brewery.configuration.locale;

import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.EventSection;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

@ConfigSerializable
public record TranslationsConfig(
        BrewSection brew,
        DistillerySection distillery,
        BarrelSection barrel,
        CauldronSection cauldron,
        CommandSection command,
        EventSection eventSection,
        InfoSection infoSection
) {

    private static TranslationsConfig instance;

    public static void load(File dataFolder) {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(new File(dataFolder, "locale/" + Config.config().language() + ".yml"))
                    .build();
            CommentedConfigurationNode node = loader.load();
            TranslationsConfig.instance = node.get(TranslationsConfig.class, new TranslationsConfig(
                    BrewSection.DEFAULT,
                    DistillerySection.DEFAULT,
                    BarrelSection.DEFAULT,
                    CauldronSection.DEFAULT,
                    CommandSection.DEFAULT,
                    EventSection.DEFAULT,
                    new InfoSection("after drink")
            ));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public static TranslationsConfig translations() {
        return instance;
    }
}
