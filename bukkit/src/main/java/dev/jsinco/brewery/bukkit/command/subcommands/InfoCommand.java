package dev.jsinco.brewery.bukkit.command.subcommands;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.command.BukkitCommandManager;
import dev.jsinco.brewery.bukkit.command.BukkitSubCommand;
import dev.jsinco.brewery.bukkit.recipe.RecipeEffects;
import dev.jsinco.brewery.bukkit.util.MessageUtil;
import dev.jsinco.brewery.command.SubCommandInfo;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@SubCommandInfo(
        name = "info",
        permission = "brewery.command.info"
)
public class InfoCommand implements BukkitSubCommand {

    @Override
    public void execute(TheBrewingProject instance, CommandSender sender, OfflinePlayer offlineTarget, String label, List<String> args) {
        Player target = toOnlineTarget(offlineTarget);
        if (target == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_UNDEFINED_PLAYER));
            return;
        }

        ItemStack item = !args.isEmpty() ? target.getInventory().getItem(Integer.parseInt(args.get(1))) : target.getInventory().getItemInMainHand();
        if (item == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_INFO_NOT_A_BREW));
            return;
        }
        Optional<Brew> brewOptional = BrewAdapter.fromItem(item);
        brewOptional
                .ifPresent(brew -> sender.sendMessage(
                        MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_INFO_BREW_MESSAGE,
                                MessageUtil.getScoreTagResolver(brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry())
                                        .map(brew::score)
                                        .orElse(BrewScoreImpl.NONE)),
                                Placeholder.component("brewing_step_info", MessageUtil.compileBrewInfo(brew, true)
                                        .collect(Component.toComponent(Component.text("\n")))
                                )
                        )
                ));
        Optional<RecipeEffects> recipeEffectsOptional = RecipeEffects.fromItem(item);
        recipeEffectsOptional.ifPresent(effects -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_INFO_EFFECT_MESSAGE, MessageUtil.recipeEffectResolver(effects)));
        });
        if (brewOptional.isEmpty() && recipeEffectsOptional.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_INFO_NOT_A_BREW));
        }
    }

    @Override
    public List<String> tabComplete(TheBrewingProject instance, CommandSender sender, OfflinePlayer target, String label, List<String> args) {
        return BukkitCommandManager.INTEGER_TAB_COMPLETIONS;
    }
}
