package dev.jsinco.brewery.api.breweries;

import java.util.concurrent.CompletableFuture;

public interface SelfSchedulingBrewery {

    /**
     * For Folia regions compatibility
     *
     * @param action The action to run
     * @return A completable future that is completed when the action completes
     */
    CompletableFuture<Void> run(Runnable action);
}
