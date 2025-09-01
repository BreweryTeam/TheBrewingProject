package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.effect.modifier.ModifierDisplay;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.effect.modifier.ModifierTooltip;
import dev.jsinco.brewery.configuration.serializers.ConsumableSerializer;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
@Header({
        "A drunken modifier represents the state of a player.",
        "Depending on the state of the player, it will experience different effects, see ''./events.yml''.",
        "This configuration file allows full customization with how to represent the state, and how it changes over time.",
        "d<modifier-name> is the difference for a modifier most probably caused by the consumption of a drink",
        "There is also a quality variable called quality, which will be a value in the range [0, 1] and defaults to 1 if none could be found"
})
public class DrunkenModifierSection extends OkaeriConfig {

    @CustomKey("drunken-modifiers")
    @Comment({"This is where you define all modifiers that can affect the player, used in drunken events.",
            "Avoid names with arithmetic operators included and names that clashes with other configuration keys,",
            "MODIFY AT YOUR OWN RISK!"})
    private List<DrunkenModifier> drunkenModifiers = List.of(
            new DrunkenModifier("alcohol", new ModifierExpression("dalcohol * (110 - alcohol_addiction) / 110"), new ModifierExpression("200"), 0D),
            new DrunkenModifier("alcohol_addiction", new ModifierExpression("0.001 * dalcohol"), new ModifierExpression("10000"), 0D),
            new DrunkenModifier("toxins", new ModifierExpression("0"), new ModifierExpression("-1"), 0D)
    );

    @CustomKey("drunken-displays")
    @Comment({
            "You can display to the player once a modifier changes",
            "type can have the values [skull, bars, stars]",
            "display-window can have the values [chat, bar title]"
    })
    private List<ModifierDisplay> drunkenDisplays = List.of(
            new ModifierDisplay(Component.text("Alcohol").color(NamedTextColor.GRAY), new ModifierExpression("alcohol"), ModifierDisplay.DisplayType.BARS, ModifierDisplay.DisplayWindow.BAR)
    );

    @CustomKey("drunken-tooltips")
    @Comment({
            "Change how modifiers will display for brew effects",
            "a drunken modifier with an expression larger than 0 will display on an item"
    })
    private List<ModifierTooltip> drunkenTooltips = List.of(
            new ModifierTooltip(new ModifierExpression("alcohol"),
                    "<lang:tbp.brew.tooltip.detailed-alcoholic>",
                    "<lang:tbp.brew.tooltip.alcoholic>",
                    "<lang:tbp.brew.tooltip.alcoholic>"
            )
    );

    @CustomKey("consumables")
    private List<ConsumableSerializer.Consumable> consumables = List.of(
            new ConsumableSerializer.Consumable("ROTTEN_FLESH", Map.of(
                    modifier("toxins"), 3D
            )),
            new ConsumableSerializer.Consumable("SPIDER_EYE", Map.of(
                    modifier("toxins"), 2D
            )),
            new ConsumableSerializer.Consumable("MILK_BUCKET", Map.of(
                    modifier("alcohol"), -3D
            )),
            new ConsumableSerializer.Consumable("BREAD", Map.of(
                    modifier("alcohol"), -2D,
                    modifier("toxins"), -1D
            ))
    );


    @Exclude
    private static DrunkenModifierSection instance;

    public static DrunkenModifierSection modifiers() {
        return instance;
    }

    public static void load(File dataFolder, OkaeriSerdesPack... packs) {
        DrunkenModifierSection.instance = ConfigManager.create(DrunkenModifierSection.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer(), packs);
            it.withBindFile(new File(dataFolder, "events.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }

    public DrunkenModifier modifier(String modifierName) {
        return optionalModifier(modifierName).orElseThrow(() -> new IllegalArgumentException("Unknown modifier: " + modifierName));
    }

    public Optional<DrunkenModifier> optionalModifier(String modifierName) {
        return drunkenModifiers.stream()
                .filter(modifier -> modifier.name().equals(modifierName))
                .findFirst();
    }
}
