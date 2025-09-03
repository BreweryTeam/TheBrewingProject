package dev.jsinco.brewery.api.brew;

import java.util.List;
import java.util.Optional;

public interface BrewManager<I> {

    /**
     * @param steps A list of steps
     * @return A new brew instance with the given steps
     */
    Brew createBrew(List<BrewingStep> steps);

    /**
     * @param cookStep A cook step
     * @return A new brew with a cook step as the initial step
     */
    Brew createBrew(BrewingStep.Cook cookStep);

    /**
     * @param mixStep A mix step
     * @return A new brew with a mix step as the initial step
     */
    Brew createBrew(BrewingStep.Mix mixStep);

    /**
     * @param brew  A brew
     * @param state A brew state
     * @return A new item stack with contents according to the brew
     */
    I toItem(Brew brew, Brew.State state);

    /**
     * @param item An item stack
     * @return An optionally present brew if item had brew contents
     */
    Optional<Brew> fromItem(I item);
}
