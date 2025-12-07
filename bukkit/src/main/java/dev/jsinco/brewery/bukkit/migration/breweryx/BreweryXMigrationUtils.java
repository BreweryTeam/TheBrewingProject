package dev.jsinco.brewery.bukkit.migration.breweryx;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Optional;

public class BreweryXMigrationUtils {

    public static ItemStack migrate(ItemStack item) {
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
            Brewdata data = loadBrewdataFromStream(in);
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
            BrewQuality quality = data.quality >= 9 ? BrewQuality.EXCELLENT : data.quality >= 6 ? BrewQuality.GOOD : BrewQuality.BAD;
            BukkitRecipeResult result = (BukkitRecipeResult) recipe.getRecipeResult(quality);
            Brew brew = TheBrewingProject.getInstance().getBrewManager().createBrew(recipe.getSteps());
            Brew.State state = data.sealed ? new Brew.State.Seal(null) : new Brew.State.Other();
            BrewScore score = brew.score(recipe);
            if (score instanceof BrewScoreImpl scoreImpl) scoreImpl.setQualityOverride(quality);
            return result.newBrewItem(score, brew, state);

        } catch (IOException | InvalidKeyException e) {
            Logger.logErr("Failed to convert a BreweryX Brew:");
            Logger.logErr(e);
        }
        return null;
    }

    private record Brewdata(String recipe, int quality, int alcohol, boolean sealed) {}
    private static Brewdata loadBrewdataFromStream(DataInputStream in) throws IOException {
        int quality = in.readByte();
        int bools = in.readUnsignedByte();

        int alcohol = 0;
        if ((bools & 64) != 0) {
            alcohol = in.readShort();
        }

        if ((bools & 1) != 0) { // distillRuns (byte)
            in.skipBytes(1);
        }
        if ((bools & 2) != 0) { // ageTime (float)
            in.skipBytes(4);
        }
        if ((bools & 4) != 0) { // woodType (float)
            in.skipBytes(4);
        }

        String recipe = null;
        if ((bools & 8) != 0) { // recipe (string)
            recipe = in.readUTF();
        }

        boolean sealed = (bools & 128) != 0;
        return new Brewdata(recipe, quality, alcohol, sealed);
    }

}
