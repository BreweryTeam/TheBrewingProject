package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.ModifierConsume;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.effect.modifier.ModifierDisplay;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConsumedModifierDisplay {
    private ConsumedModifierDisplay() {}

    public static void renderConsumeDisplay(Player player, ModifierDisplay.DisplayWindow displayWindow,
                                            @Nullable DrunkState beforeState, @Nullable DrunkState afterState,
                                            List<ModifierConsume> consumedModifiers) {
        Map<DrunkenModifier, Double> consumedModifiersMap = consumedModifiers.stream().collect(Collectors.toMap(
                ModifierConsume::modifier, ModifierConsume::value
        ));
        renderConsumeDisplay(player, displayWindow, beforeState, afterState, consumedModifiersMap);
    }

    public static void renderConsumeDisplay(Player player, ModifierDisplay.DisplayWindow displayWindow,
                                            @Nullable DrunkState beforeState, @Nullable DrunkState afterState,
                                            Map<DrunkenModifier, Double> consumedModifiers) {
        List<DrunkenModifier> changed = modifiersChanged(beforeState, afterState);
        if (changed.isEmpty()) {
            return;
        }
        Map<String, Double> variables = (afterState == null ? new DrunkStateImpl(0, -1) : afterState).asVariables();
        consumedModifiers.entrySet().stream()
                .filter(entry -> changed.contains(entry.getKey()))
                .forEach(entry ->  variables.put("consumed_" + entry.getKey().name(), entry.getValue()));
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

    private static List<DrunkenModifier> modifiersChanged(@Nullable DrunkState beforeState, @Nullable DrunkState afterState) {
        if (beforeState != null && afterState != null) {
            Map<DrunkenModifier, Double> before = beforeState.modifiers();
            Map<DrunkenModifier, Double> after = afterState.modifiers();
            return beforeState.modifiers().keySet().stream()
                    .filter(modifier -> !Objects.equals(before.get(modifier), after.get(modifier)))
                    .toList();
        }
        if (beforeState != null) {
            return beforeState.modifiers().entrySet().stream()
                    .filter(entry -> entry.getValue() != entry.getKey().minValue())
                    .map(Map.Entry::getKey)
                    .toList();
        }
        if (afterState != null) {
            return afterState.modifiers().entrySet().stream()
                    .filter(entry -> entry.getValue() != entry.getKey().minValue())
                    .map(Map.Entry::getKey)
                    .toList();
        }
        return List.of();
    }

}
