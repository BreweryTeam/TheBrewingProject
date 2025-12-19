package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.DrunksManager;
import dev.jsinco.brewery.api.effect.ModifierConsume;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.effect.modifier.ModifierDisplay;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsumedModifierDisplay {

    public static void renderConsumeDisplay(Player player, ModifierDisplay.DisplayWindow displayWindow,
                                            DrunksManager drunksManager, List<ModifierConsume> consumedModifiers) {
        Map<DrunkenModifier, Double> consumedModifiersMap = consumedModifiers.stream().collect(Collectors.toMap(
                ModifierConsume::modifier, ModifierConsume::value
        ));
        renderConsumeDisplay(player, displayWindow, drunksManager, consumedModifiersMap);
    }
    public static void renderConsumeDisplay(Player player, ModifierDisplay.DisplayWindow displayWindow,
                                            DrunksManager drunksManager, Map<DrunkenModifier, Double> consumedModifiers) {
        DrunkState drunkState = drunksManager.getDrunkState(player.getUniqueId());
        Map<String, Double> variables = (drunkState == null ? new DrunkStateImpl(0, -1) : drunkState).asVariables();
        consumedModifiers.forEach((modifier, value) -> variables.put("consumed_" + modifier.name(), value));
        Component component = DrunkenModifierSection.modifiers().drunkenDisplays()
                .stream()
                .filter(modifierDisplay -> modifierDisplay.displayWindow().equals(displayWindow))
                .filter(modifierDisplay -> modifierDisplay.filter().evaluate(variables) > 0)
                .map(modifierDisplay -> MessageUtil.miniMessage(
                        modifierDisplay.message(),
                        MessageUtil.getValueDisplayTagResolver(modifierDisplay.value().evaluate(variables)))
                )
                .collect(Component.toComponent(Component.text(", ")));
        if (component.equals(Component.empty())) {
            return;
        }
        switch (displayWindow) {
            case CHAT -> player.sendMessage(component);
            case ACTION_BAR -> player.sendActionBar(component);
            case TITLE -> player.showTitle(Title.title(component, Component.empty()));
        }
    }

}
