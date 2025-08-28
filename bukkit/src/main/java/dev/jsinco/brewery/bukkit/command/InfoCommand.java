package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.command.argument.EnumArgument;
import dev.jsinco.brewery.bukkit.recipe.RecipeEffects;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class InfoCommand {
    private static final int PLAYER_INVENTORY_SIZE = 41;

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        ArgumentBuilder<CommandSourceStack, ?> withIndex = Commands.argument("inventory_slot", IntegerArgumentType.integer(0, PLAYER_INVENTORY_SIZE - 1))
                .executes(context -> {
                    Player target = BreweryCommand.getPlayer(context);
                    int slot = context.getArgument("inventory_slot", int.class);
                    PlayerInventory inventory = target.getInventory();
                    showInfo(inventory.getItem(slot), context.getSource().getSender());
                    return 1;
                });
        ArgumentBuilder<CommandSourceStack, ?> withNamedSlot = Commands.argument("equipment_slot", new EnumArgument<>(EquipmentSlot.class))
                .executes(context -> {
                    Player target = BreweryCommand.getPlayer(context);
                    PlayerInventory inventory = target.getInventory();
                    showInfo(inventory.getItem(context.getArgument("equipment_slot", EquipmentSlot.class)), context.getSource().getSender());
                    return 1;
                });
        return Commands.literal("info")
                .then(withNamedSlot)
                .then(withIndex)
                .then(BreweryCommand.playerBranch(argument -> {
                    argument.then(withNamedSlot);
                    argument.then(withIndex);
                    argument.executes(InfoCommand::showHeldItemInfo);
                }))
                .executes(InfoCommand::showHeldItemInfo);
    }

    private static int showHeldItemInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player target = BreweryCommand.getPlayer(context);
        PlayerInventory inventory = target.getInventory();
        showInfo(inventory.getItemInMainHand(), context.getSource().getSender());
        return 1;
    }

    private static void showInfo(@Nullable ItemStack itemStack, CommandSender sender) {
        if (itemStack == null) {
            MessageUtil.message(sender, "tbp.command.info.not-a-brew");
            return;
        }
        Optional<Brew> brewOptional = BrewAdapter.fromItem(itemStack);
        brewOptional
                .ifPresent(brew -> MessageUtil.message(sender,
                        "tbp.command.info.message",
                        MessageUtil.getScoreTagResolver(brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry())
                                .map(brew::score)
                                .orElse(BrewScoreImpl.failed(brew))),
                        Placeholder.component("brewing_step_info", MessageUtil.compileBrewInfo(brew, true, TheBrewingProject.getInstance().getRecipeRegistry())
                                .collect(Component.toComponent(Component.text("\n")))
                        ))
                );
        Optional<RecipeEffects> recipeEffectsOptional = RecipeEffects.fromItem(itemStack);
        recipeEffectsOptional.ifPresent(effects -> {
            MessageUtil.message(sender, "tbp.command.info.effect-message", BukkitMessageUtil.recipeEffectResolver(effects));
        });
        if (brewOptional.isEmpty() && recipeEffectsOptional.isEmpty()) {
            MessageUtil.message(sender, "tbp.command.info.not-a-brew");
        }
    }
}
