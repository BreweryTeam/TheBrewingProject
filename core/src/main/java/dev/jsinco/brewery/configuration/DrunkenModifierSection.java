package dev.jsinco.brewery.configuration;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.effect.modifier.ModifierDisplay;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.effect.modifier.ModifierTooltip;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.configuration.serializers.ConsumableSerializer;
import dev.jsinco.brewery.effect.DrunkStateImpl;
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

import java.io.File;
import java.util.*;

@Getter
@Accessors(fluent = true)
@Header({
        "A drunken modifier represents the state of a player.",
        "Depending on the state of the player, it will experience different effects, see ''./events.yml''.",
        "This configuration file allows full customization with how to represent the state, and how it changes over time.",
        "consumed_<modifier-name> is only a non zero variable when something is consumed"
})
public class DrunkenModifierSection extends OkaeriConfig {

    @CustomKey("drunken-modifiers")
    @Comment({"This is where you define all modifiers that can affect the player, used in drunken events.",
            "Avoid names with arithmetic operators included and names that clashes with other configuration keys,",
    })
    private List<DrunkenModifier> drunkenModifiers = List.of(
            new DrunkenModifier("alcohol", new ModifierExpression("consumed_alcohol * (110 - alcohol_addiction) / 110"), new ModifierExpression("400"), 0D, 100D, Component.text("Alcohol")),
            new DrunkenModifier("alcohol_addiction", new ModifierExpression("0.001 * consumed_alcohol"), new ModifierExpression("10000"), 0D, 100D, Component.text("Alcohol addiction")),
            new DrunkenModifier("toxins", new ModifierExpression("consumed_toxins"), new ModifierExpression("800"), 0D, 100D, Component.text("Toxins"))
    );

    @CustomKey("drunken-displays")
    @Comment({
            "You can display to the player once a modifier changes from consumption",
            "There's 3 different tags you can use to display the value: [skull, bars, stars]",
            "display-window can have the values [chat, action_bar title]"
    })
    private List<ModifierDisplay> drunkenDisplays = List.of(
            new ModifierDisplay("<gray>Alcohol: <red>[<bars>]", new ModifierExpression("abs(consumed_alcohol)"), new ModifierExpression("alcohol"), ModifierDisplay.DisplayWindow.ACTION_BAR),
            new ModifierDisplay("<green>Toxins: [<skulls>]", new ModifierExpression("abs(consumed_toxins)"), new ModifierExpression("toxins"), ModifierDisplay.DisplayWindow.ACTION_BAR)
    );

    @CustomKey("drunken-tooltips")
    @Comment({
            "Change how modifiers will display for brew effects",
            "a drunken modifier with a filter larger than 0 will display on an item"
    })
    private List<ModifierTooltip> drunkenTooltips = List.of(
            new ModifierTooltip(new ModifierExpression("alcohol"),
                    "<lang:tbp.brew.tooltip.detailed-alcoholic:'<alcohol>'>",
                    "<lang:tbp.brew.tooltip.alcoholic>",
                    "<lang:tbp.brew.tooltip.alcoholic>"
            )
    );

    @CustomKey("consumables")
    @Comment({
            "Add some modifier change behavior to vanilla items"
    })
    private List<ConsumableSerializer.Consumable> consumables = List.of(
            new ConsumableSerializer.Consumable("rotten_flesh", Map.of(
                    "toxins", 3D
            )),
            new ConsumableSerializer.Consumable("spider_eye", Map.of(
                    "toxins", 2D
            )),
            new ConsumableSerializer.Consumable("milk_bucket", Map.of(
                    "alcohol", -3D
            )),
            new ConsumableSerializer.Consumable("bread", Map.of(
                    "alcohol", -2D,
                    "toxins", -1D
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
            it.withBindFile(new File(dataFolder, "modifiers.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }

    public static void validate() {
        Preconditions.checkState(instance != null, "Instance can not be null");
        Map<String, Double> variables = new DrunkStateImpl(0, -1).asVariables();
        boolean noneFailed = true;
        for (DrunkenModifier drunkenModifier : instance.drunkenModifiers()) {
            try {
                drunkenModifier.decrementTime().evaluate(variables);
                drunkenModifier.dependency().evaluate(variables);
            } catch (Exception e) {
                Logger.logErr("Failed to validate modifier: " + drunkenModifier.name());
                Logger.logErr(e);
                noneFailed = false;
            }
        }
        for (ModifierDisplay modifierDisplay : instance.drunkenDisplays()) {
            try {
                modifierDisplay.filter().evaluate(variables);
                modifierDisplay.value().evaluate(variables);
            } catch (Exception e) {
                Logger.logErr(e);
                noneFailed = false;
            }
        }
        for (ModifierTooltip modifierTooltip : instance.drunkenTooltips()) {
            try {
                modifierTooltip.filter().evaluate(variables);
            } catch (Exception e) {
                Logger.logErr(e);
                noneFailed = false;
            }
        }
        Preconditions.checkState(noneFailed, "Encountered an issue when validating modifiers, see above exception");
        Set<String> modifierNames = new HashSet<>();
        Set<String> clashes = new HashSet<>();
        for (DrunkenModifier drunkenModifier : instance.drunkenModifiers()) {
            if (modifierNames.contains(drunkenModifier.name())) {
                clashes.add(drunkenModifier.name());
                continue;
            }
            modifierNames.add(drunkenModifier.name());
        }
        Preconditions.checkState(clashes.isEmpty(), "The following modifiers have the same name: " + clashes);
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
