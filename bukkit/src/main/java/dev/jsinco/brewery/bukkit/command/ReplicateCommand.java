package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.command.argument.RecipeArgument;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReplicateCommand {


    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        return Commands.argument("recipe-name", new RecipeArgument())
                .executes(context -> {
                    Recipe<ItemStack> recipe = context.getArgument("recipe-name", Recipe.class);
                    Player target = BreweryCommand.getPlayer(context);
                    Brew brew = new BrewImpl(recipe.getSteps());
                    ItemStack brewItem = BrewAdapter.toItem(brew, new Brew.State.Other());
                    if (!target.getInventory().addItem(brewItem).isEmpty()) {
                        target.getLocation().getWorld().dropItem(target.getLocation(), brewItem);
                    }
                    MessageUtil.message(context.getSource().getSender(), "tbp.command.create.success", Placeholder.component("brew_name", brewItem.effectiveName()));
                    return 1;
                });
    }
}
