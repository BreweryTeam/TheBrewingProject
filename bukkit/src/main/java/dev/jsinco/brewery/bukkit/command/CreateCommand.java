package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.brew.*;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.command.argument.EnumArgument;
import dev.jsinco.brewery.bukkit.command.argument.FlaggedArgumentBuilder;
import dev.jsinco.brewery.bukkit.command.argument.IngredientsArgument;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.moment.PassedMoment;
import dev.jsinco.brewery.util.Pair;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        FlaggedArgumentBuilder.Flag cook = new FlaggedArgumentBuilder.Flag("cook", "c", List.of(
                new Pair<>("cook-time", DoubleArgumentType.doubleArg(0)),
                new Pair<>("cook-type", new EnumArgument<>(CauldronType.class)),
                new Pair<>("cook-ingredients", new IngredientsArgument())
        ), Set.of(FlaggedArgumentBuilder.FlagProperty.MANDATORY_FIRST, FlaggedArgumentBuilder.FlagProperty.ONLY_FIRST));
        FlaggedArgumentBuilder.Flag mix = new FlaggedArgumentBuilder.Flag("mix", "m", List.of(
                new Pair<>("mix-time", DoubleArgumentType.doubleArg(0)),
                new Pair<>("mix-ingredients", new IngredientsArgument())
        ), Set.of(FlaggedArgumentBuilder.FlagProperty.MANDATORY_FIRST, FlaggedArgumentBuilder.FlagProperty.ONLY_FIRST));
        FlaggedArgumentBuilder.Flag distill = new FlaggedArgumentBuilder.Flag("distill", "d", List.of(
                new Pair<>("distill-runs", IntegerArgumentType.integer(1))
        ), Set.of());
        FlaggedArgumentBuilder.Flag age = new FlaggedArgumentBuilder.Flag("age", "a", List.of(
                new Pair<>("barrel-type", new EnumArgument<>(BarrelType.class)),
                new Pair<>("aging-years", DoubleArgumentType.doubleArg(0))
        ), Set.of());
        List<ArgumentBuilder<CommandSourceStack, ?>> tree = new FlaggedArgumentBuilder(Set.of(cook, mix, distill, age), (context, flags) -> {
            List<BrewingStep> steps = new ArrayList<>();
            for (FlaggedArgumentBuilder.Flag flag : flags) {
                if (flag == cook) {
                    steps.add(parseCook(context));
                } else if (flag == mix) {
                    steps.add(parseMix(context));
                } else if (flag == distill) {
                    steps.add(parseDistill(context));
                } else if (flag == age) {
                    steps.add(parseAge(context));
                }
            }
            Player target = BreweryCommand.getPlayer(context);
            ItemStack brewItem = BrewAdapter.toItem(new BrewImpl(steps), new BrewImpl.State.Other());
            PlayerInventory inventory = target.getInventory();
            if (!inventory.addItem(brewItem).isEmpty()) {
                target.getWorld().dropItem(target.getLocation(), brewItem);
            }
            ItemMeta brewItemMeta = brewItem.getItemMeta();
            context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_CREATE_SUCCESS, Placeholder.component("brew_name", brewItemMeta.hasCustomName() ? brewItemMeta.customName() : brewItemMeta.itemName())));
        }).build();
        ArgumentBuilder<CommandSourceStack, ?> root = Commands.literal("create");
        tree.forEach(root::then);
        root.then(BreweryCommand.playerBranch(argument -> tree.forEach(argument::then)));
        return root;
    }

    private static BrewingStep parseAge(CommandContext<CommandSourceStack> context) {
        double agingYears = context.getArgument("aging-years", double.class);
        return new AgeStepImpl(new PassedMoment((long) (agingYears * Moment.AGING_YEAR)), context.getArgument("barrel-type", BarrelType.class));
    }

    private static BrewingStep parseDistill(CommandContext<CommandSourceStack> context) {
        return new DistillStepImpl(context.getArgument("distill-runs", int.class));
    }

    private static BrewingStep parseMix(CommandContext<CommandSourceStack> context) {
        double mixTIme = context.getArgument("mix-time", double.class);
        Map<Ingredient, Integer> ingredients = context.getArgument("mix-ingredients", Map.class);
        return new MixStepImpl(new PassedMoment((long) (mixTIme * Moment.MINUTE)), ingredients);
    }

    private static BrewingStep parseCook(CommandContext<CommandSourceStack> context) {
        double cookTime = context.getArgument("cook-time", double.class);
        CauldronType cauldronType = context.getArgument("cook-type", CauldronType.class);
        Map<Ingredient, Integer> ingredients = context.getArgument("cook-ingredients", Map.class);
        return new CookStepImpl(new PassedMoment((long) (cookTime * Moment.MINUTE)), ingredients, cauldronType);
    }
}
