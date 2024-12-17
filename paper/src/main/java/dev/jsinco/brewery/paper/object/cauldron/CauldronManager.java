package dev.jsinco.brewery.paper.object.cauldron;

import com.github.puregero.multilib.MultiLib;
import com.github.puregero.multilib.regionized.RegionizedTask;
import dev.jsinco.brewery.paper.TheBrewingProject;
import dev.jsinco.brewery.api.math.BlockPos;
import dev.jsinco.brewery.api.object.cauldron.Cauldron;

import java.util.ArrayList;
import java.util.List;

public class CauldronManager {
    private final List<BukkitCauldron> cauldrons = new ArrayList<>();
    private final RegionizedTask cookTickTask;
    private final RegionizedTask asyncTickTask;

    public CauldronManager() {
        // TODO: Support changing the frequency here
        this.cookTickTask = MultiLib.getGlobalRegionScheduler().runAtFixedRate(TheBrewingProject.getInstance(), (task) -> cookTickAll(), 1L, 20L * 60L);
        this.asyncTickTask = MultiLib.getAsyncScheduler().runAtFixedRate(TheBrewingProject.getInstance(), (task) -> asyncTickAll(), 1L, 1L);
    }

    public void disable() {
        cookTickTask.cancel();
        asyncTickTask.cancel();
    }

    public void addCauldron(BukkitCauldron cauldron) {
        this.cauldrons.add(cauldron);
    }

    /**
     * Removes cauldron
     *
     * @param cauldron cauldron to remove
     */
    public void removeCauldron(BukkitCauldron cauldron) {
        this.cauldrons.remove(cauldron);
    }

    /**
     * Removes cauldron with the provided position
     *
     * @param blockPos the position
     */
    public void removeCauldron(BlockPos blockPos) {
        this.cauldrons.removeIf(cauldron -> cauldron.getPosition().equals(blockPos));
    }

    /**
     *
     */
    public Cauldron ensureAndGetCauldron(BlockPos blockPos) {
        for (var cauldron : cauldrons) {
            if (cauldron.getPosition().equals(blockPos)) {
                return cauldron;
            }
        }

        BukkitCauldron cauldron = new BukkitCauldron(blockPos);
        cauldrons.add(cauldron);

        return cauldron;
    }

    public void cookTickAll() {
        for (Cauldron cauldron : cauldrons) {
            cauldron.handleCookTick();
        }
    }

    public void asyncTickAll() {
        for (Cauldron cauldron : cauldrons) {
            cauldron.handleAsyncTick();
        }
    }
}
