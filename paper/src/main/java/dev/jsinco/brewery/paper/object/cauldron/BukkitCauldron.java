package dev.jsinco.brewery.paper.object.cauldron;

import dev.jsinco.brewery.api.math.BlockPos;
import dev.jsinco.brewery.api.object.cauldron.Cauldron;
import dev.jsinco.brewery.api.recipe.BrewResult;
import lombok.Getter;
import org.bukkit.Particle;

import java.util.Random;

@Getter
public class BukkitCauldron extends Cauldron {
    private static final Random RANDOM = new Random();

    private final BlockPos position;

    public BukkitCauldron(BlockPos position) {
        super();
        
        this.position = position;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BukkitCauldron cauldron = (BukkitCauldron) obj;
        return getUid().equals(cauldron.getUid());
    }

    @Override
    public void handleCookTick() {
        // if (theBlockBelowIsAHeatingSource(currentRecipe))
        this.increaseCookTime();
    }

    @Override
    public void handleAsyncTick() {
        // particles and stuff
        position.getLocation().getWorld().spawnParticle(
                Particle.FLAME,
                position.getLocation().add(0.0, 0.5, 0.0),
                5,
                0.2,
                0,
                0.2
        );
    }

    @Override
    public BrewResult getBrewResult() {
        return null;
    }
}
