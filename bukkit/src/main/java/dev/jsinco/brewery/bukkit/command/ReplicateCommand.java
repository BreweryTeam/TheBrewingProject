package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.command.argument.EnumArgument;
import dev.jsinco.brewery.bukkit.command.argument.RecipeArgument;
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReplicateCommand {


    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        return Commands.argument("recipe-name", new RecipeArgument())
                .then(Commands.argument("quality", new EnumArgument<>(BrewQuality.class))
                        .executes(context -> {
                            Recipe<ItemStack> recipe = context.getArgument("recipe-name", Recipe.class);
                            BrewQuality quality = context.getArgument("quality", BrewQuality.class);
                            Player target = BreweryCommand.getPlayer(context);
                            givePlayerBrew(recipe, context, target, quality);
                            return 1;
                        })
                )
                .executes(context -> {
                    Recipe<ItemStack> recipe = context.getArgument("recipe-name", Recipe.class);
                    Player target = BreweryCommand.getPlayer(context);
                    givePlayerBrew(recipe, context, target, BrewQuality.EXCELLENT);
                    return 1;
                });
    }

    private static void givePlayerBrew(Recipe<ItemStack> recipe, CommandContext<CommandSourceStack> context, Player target, BrewQuality quality) {
        Brew brew = new BrewImpl(recipe.getSteps());
        BrewScore score = brew.score(recipe);
        if (score instanceof BrewScoreImpl scoreImpl) scoreImpl.setQualityOverride(quality);
        ItemStack brewItem = recipe.getRecipeResult(quality).newBrewItem(score, brew, new Brew.State.Other());
        brewItem.editPersistentDataContainer(pdc -> {
            BrewAdapter.applyBrewTags(pdc, recipe, score.score(), ((BukkitRecipeResult) recipe.getRecipeResult(quality)).getName());
            BrewAdapter.applyBrewStepsData(pdc, brew);
        });
        if (!target.getInventory().addItem(brewItem).isEmpty()) {
            target.getLocation().getWorld().dropItem(target.getLocation(), brewItem);
        }
        MessageUtil.message(context.getSource().getSender(), "tbp.command.create.success", Placeholder.component("brew_name", brewItem.effectiveName()));
    }
}
