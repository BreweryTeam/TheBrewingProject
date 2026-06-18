package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewManager;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.api.recipe.RecipeMatcher;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.brew.MixStepImpl;
import dev.jsinco.brewery.bukkit.recipe.RecipeMatcherImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BukkitBrewManager implements BrewManager<ItemStack> {
    @Override
    public Brew createBrew(List<BrewingStep> steps) {
        return new BrewImpl(steps);
    }

    @Override
    public Brew createBrew(List<BrewingStep> steps, MetaData meta) {
        return new BrewImpl(steps, meta);
    }

    @Override
    public Brew createBrew(BrewingStep.Cook cookStep) {
        return new BrewImpl(cookStep);
    }

    @Override
    public Brew createBrew(BrewingStep.Mix mixStep) {
        return new BrewImpl(mixStep);
    }

    @Override
    public ItemStack toItem(Brew brew, Brew.State brewState) {
        return BrewAdapterAccess.toItem(brew, brewState);
    }

    @Override
    public Optional<Brew> fromItem(ItemStack item) {
        return BrewAdapterAccess.fromItem(item);
    }

    @Override
    public BrewingStep.Cook cookingStep(long cookingTicks, Map<? extends Ingredient, Integer> ingredients, CauldronType cauldronType) {
        return new CookStepImpl(
                new PassedMoment(cookingTicks),
                ingredients,
                cauldronType
        );
    }

    @Override
    public BrewingStep.Mix mixingStep(long mixingTicks, Map<? extends Ingredient, Integer> ingredients, CauldronType cauldronType) {
        return new MixStepImpl(
                new PassedMoment(mixingTicks),
                ingredients,
                cauldronType
        );
    }

    @Override
    public BrewingStep.Age agingStep(long agingTicks, BarrelType barrelType) {
        return new AgeStepImpl(
                new PassedMoment(agingTicks),
                barrelType
        );
    }

    @Override
    public BrewingStep.Distill distillStep(int runs) {
        return new DistillStepImpl(runs);
    }

    @Override
    public Optional<String> brewName(ItemStack itemStack) {
        return Optional.ofNullable(
                itemStack.getPersistentDataContainer().get(BrewAdapterAccess.BREWERY_TAG, PersistentDataType.STRING)
        );
    }

    @Override
    public Optional<Component> brewDisplayName(ItemStack itemStack) {
        return Optional.ofNullable(
                itemStack.getPersistentDataContainer().get(BrewAdapterAccess.BREWERY_DISPLAY_NAME, PersistentDataType.STRING)
        ).map(MiniMessage.miniMessage()::deserialize);
    }

    @Override
    public Optional<BrewQuality> brewQuality(ItemStack itemStack) {
        return brewScore(itemStack)
                .flatMap(BrewQuality::quality);
    }

    @Override
    public Optional<Double> brewScore(ItemStack itemStack) {
        return Optional.ofNullable(
                itemStack.getPersistentDataContainer().get(BrewAdapterAccess.BREWERY_SCORE, PersistentDataType.DOUBLE)
        );
    }

    @Override
    public RecipeMatcher.Builder<ItemStack> matcher() {
        return new RecipeMatcherImpl.BuilderImpl();
    }
}
