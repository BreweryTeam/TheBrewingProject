package dev.jsinco.brewery.bukkit.migration.breweryx;

import dev.jsinco.brewery.api.brew.*;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.ingredient.SimpleIngredient;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.*;

public class BreweryXMigrationUtils {

    private static final List<BarrelType> BARREL_TYPES = List.of(
            BarrelType.ANY,
            BarrelType.BIRCH,
            BarrelType.OAK,
            BarrelType.JUNGLE,
            BarrelType.SPRUCE,
            BarrelType.ACACIA,
            BarrelType.DARK_OAK,
            BarrelType.CRIMSON,
            BarrelType.WARPED,
            BarrelType.MANGROVE,
            BarrelType.CHERRY,
            BarrelType.BAMBOO,
            BarrelType.COPPER,
            BarrelType.PALE_OAK
    );

    public static @Nullable ItemStack migrate(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        NBTLoadStream nbtStream = new NBTLoadStream(meta);
        if (!nbtStream.hasData()) {
            return null;
        }

        XORUnscrambleStream unscrambler = new XORUnscrambleStream(nbtStream, Config.config().breweryxMigrationSeeds());
        try (DataInputStream in = new DataInputStream(unscrambler)) {
            if (in.readByte() != 86) {
                Logger.logErr("Parity check failed on BreweryX Brew while migrating. Trying to load it anyway...");
            }
            if (in.readByte() != 1) {
                Logger.log("Trying to convert a BreweryX Brew that stores data in an unsupported version...");
            }

            unscrambler.start();
            BrewData data = loadBrewdataFromStream(in);
            BrewManager<ItemStack> brewManager = TheBrewingProject.getInstance().getBrewManager();
            Brew.State state = data.sealed ? new Brew.State.Seal(null) : new Brew.State.Other();
            if (data.brew == null) {
                return itemFromDataWithoutSteps(data, brewManager, state);
            }
            return brewManager.toItem(data.brew, state);

        } catch (IOException | InvalidKeyException e) {
            Logger.logErr("Failed to convert a BreweryX Brew:");
            Logger.logErr(e);
        }
        return null;
    }

    private static ItemStack itemFromDataWithoutSteps(BrewData data, BrewManager<ItemStack> brewManager, Brew.State state) {
        if (data.recipe == null) {
            Logger.logErr("Failed to convert a BreweryX Brew: Couldn't extract recipe identifier.");
            return null;
        }
        Optional<Recipe<ItemStack>> recipeOptional = TheBrewingProject.getInstance().getRecipeRegistry().getRecipe(data.recipe);
        if (recipeOptional.isEmpty()) {
            Logger.logErr("Failed to convert a BreweryX Brew: Recipe '" + data.recipe + "' not configured in TBP.");
            return null;
        }
        Recipe<ItemStack> recipe = recipeOptional.get();
        Brew brew = brewManager.createBrew(recipe.getSteps());
        BrewScore score = brew.score(recipe);
        if (score instanceof BrewScoreImpl scoreImpl) {
            scoreImpl.setQualityOverride(data.quality >= 9 ?
                    BrewQuality.EXCELLENT : data.quality >= 6 ? BrewQuality.GOOD : BrewQuality.BAD);
        }
        return brewManager.toItem(brew, state);
    }

    private record BrewData(@Nullable Brew brew, @Nullable String recipe, boolean sealed, byte quality) {
    }

    private static BrewData loadBrewdataFromStream(DataInputStream in) throws IOException {
        byte quality = in.readByte();
        int flags = in.readUnsignedByte();

        if ((flags & 64) != 0) {
            in.skipBytes(2); // Alcohol ignored (short)
        }
        List<BrewingStep> steps = new ArrayList<>();
        if ((flags & 1) != 0) {
            steps.add(new DistillStepImpl(in.readByte()));
        }
        double ageTime;
        if ((flags & 2) != 0) {
            ageTime = in.readFloat();
        } else {
            ageTime = 0;
        }
        int woodType;
        if ((flags & 4) != 0) {
            woodType = (int) in.readFloat();
        } else {
            woodType = 0;
        }
        if (ageTime != 0) {
            long ageInTicks = (long) (Config.config().barrels().agingYearTicks() * ageTime);
            BarrelType barrelType = woodType >= BARREL_TYPES.size() || woodType < 0 ?
                    BarrelType.ANY : BARREL_TYPES.get(woodType);
            steps.add(new AgeStepImpl(
                    new PassedMoment(ageInTicks),
                    barrelType
            ));
        }
        String recipe = null;
        if ((flags & 8) != 0) {
            recipe = in.readUTF();
        }
        boolean sealed = (flags & 128) != 0;
        int cookingTime = in.readInt();
        byte ingredientAmount = in.readByte();
        if (ingredientAmount == 0) {
            // Probably no data, brew is sealed
            return new BrewData(null, recipe, true, quality);
        }
        Map<Ingredient, Integer> ingredients = new HashMap<>();
        for (int i = 0; i < ingredientAmount; i++) {
            Ingredient ingredient = readIngredient(in);
            int amount = in.readShort();
            if (ingredient == null) {
                continue;
            }
            ingredients.put(ingredient, amount);
        }
        steps.addFirst(new CookStepImpl(
                new PassedMoment(cookingTime),
                ingredients,
                CauldronType.WATER
        ));
        return new BrewData(new BrewImpl(steps), recipe, sealed, quality);
    }

    private static Ingredient readIngredient(DataInputStream in) throws IOException {
        return switch (in.readUTF().toUpperCase(Locale.ROOT)) {
            case "CI" -> readCustomItem(in);
            case "PI" -> readPluginItem(in);
            case "SI" -> readSimpleItem(in);
            // Has to fail, as the following data would be messed up
            default -> throw new IOException("Unsupported ingredient type: " + in);
        };
    }

    private static Ingredient readSimpleItem(DataInputStream in) throws IOException {
        String materialName = in.readUTF();
        in.skipBytes(2); // Dur ignored (short)
        return parseSimpleIngredientWithLogMessage(materialName);
    }

    private static Ingredient parseSimpleIngredientWithLogMessage(String materialString) {
        try {
            return new SimpleIngredient(Material.valueOf(materialString));
        } catch (IllegalArgumentException e) {
            Logger.logWarn("Unknown material name for simple ingredient: " + materialString);
            return null;
        }
    }

    private static Ingredient readPluginItem(DataInputStream in) throws IOException {
        String pluginId = in.readUTF();
        String itemId = in.readUTF();
        Ingredient output = TheBrewingProject.getInstance()
                .getIntegrationManager()
                .retrieve(IntegrationTypes.ITEM)
                .stream()
                .filter(itemIntegration -> pluginId.equalsIgnoreCase(itemIntegration.getId()))
                .map(itemIntegration -> itemIntegration.createIngredient(itemId).join())
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(null);
        if (output == null) {
            Logger.logWarn("Unknown plugin id or item id for plugin ingredient: " + pluginId + ":" + itemId);
        }
        return output;
    }

    private static Ingredient readCustomItem(DataInputStream in) throws IOException {
        if (in.readBoolean()) { // Material based
            return parseSimpleIngredientWithLogMessage(in.readUTF());
        }
        if (in.readBoolean()) { // Name based (unsafe)
            in.readUTF(); // Name ignored
        }
        short loreSize = in.readShort();
        if (loreSize > 0) { // Lore based (unsafe)
            for (int i = 0; i < loreSize; i++) {
                in.readUTF(); // Lore line ignored
            }
        }
        if (in.readBoolean()) { // Custom model data (unsupported)
            in.skipBytes(4); // Int ignored
        }
        return null;
    }

}
