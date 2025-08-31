package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.effect.modifier.ModifierDisplay;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
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
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
public class DrunkenModifierSection extends OkaeriConfig {

    @CustomKey("drunken-modifiers")
    @Comment("This is where you define all modifiers that can affect the player, used in drunken events. Names including arithmetic operators are forbidden")
    private List<DrunkenModifier> drunkenModifiers = List.of(
            new DrunkenModifier("alcohol", new ModifierExpression("0"), new ModifierExpression("0"), 0D),
            new DrunkenModifier("blood_alcohol", new ModifierExpression("dalcohol * (110 - alcohol_addiction) / 110"), new ModifierExpression("200"), 0D),
            new DrunkenModifier("alcohol_addiction", new ModifierExpression("0.001 * dalcohol"), new ModifierExpression("10000"), 0D)
    );

    @CustomKey("drunken-displays")
    @Comment({
            "You can display to the player once a modifier changes",
            "type can have the values [skull, bars, stars]",
            "display-window can have the values [chat, bar title]"
    })
    private List<ModifierDisplay> drunkenDisplays = List.of(
            new ModifierDisplay(Component.text("Alcohol").color(NamedTextColor.GRAY), new ModifierExpression("blood_alcohol"), ModifierDisplay.DisplayType.BARS, ModifierDisplay.DisplayWindow.BAR)
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
