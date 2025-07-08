package dev.jsinco.brewery.bukkit.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.ingredient.Ingredient;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

import java.util.Arrays;
import java.util.Map;

public class IngredientsArgument implements CustomArgumentType.Converted<Map<Ingredient, Integer>, String> {
    @Override
    public Map<Ingredient, Integer> convert(String nativeType) throws CommandSyntaxException {
        return BukkitIngredientManager.INSTANCE.getIngredientsWithAmount(
                Arrays.stream(nativeType.split("[, ]"))
                        .map(String::strip)
                        .filter(string -> !string.isBlank())
                        .toList()
        ).join();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
