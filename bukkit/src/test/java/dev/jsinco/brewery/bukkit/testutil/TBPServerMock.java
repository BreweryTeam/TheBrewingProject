package dev.jsinco.brewery.bukkit.testutil;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.exception.UnimplementedOperationException;

import java.util.function.Consumer;

/**
 * A server mock that implements just enough dummy methods for the plugin to load in tests.
 * <strong>Scheduled tasks will not run.</strong>
 */
public class TBPServerMock extends ServerMock {

    @Override
    public @NotNull GlobalRegionScheduler getGlobalRegionScheduler() {
        return new GlobalRegionSchedulerMock();
    }

    // Good enough for 99% of plugin functionality, simulating ticking will require a proper impl
    private static class GlobalRegionSchedulerMock implements GlobalRegionScheduler {
        @Override
        public void execute(@NotNull Plugin plugin, @NotNull Runnable run) {
            throw new UnimplementedOperationException("Cannot run tasks with TBPServerMock");
        }

        @Override
        public @NotNull ScheduledTask run(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task) {
            return null;
        }

        @Override
        public @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long delayTicks) {
            return null;
        }

        @Override
        public @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long initialDelayTicks, long periodTicks) {
            return null;
        }

        @Override
        public void cancelTasks(@NotNull Plugin plugin) {
            throw new UnimplementedOperationException("Cannot cancel tasks with TBPServerMock");
        }
    }

}
