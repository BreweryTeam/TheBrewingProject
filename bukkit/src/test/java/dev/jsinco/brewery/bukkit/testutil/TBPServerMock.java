package dev.jsinco.brewery.bukkit.testutil;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.exception.UnimplementedOperationException;

import java.util.function.Consumer;

/**
 * A server mock that implements just enough dummy methods for the plugin to load in tests.
 * <strong>Scheduled tasks will not run.</strong>
 */
public class TBPServerMock extends ServerMock {

    @Override
    public @NonNull GlobalRegionScheduler getGlobalRegionScheduler() {
        return new GlobalRegionSchedulerMock();
    }

    // Good enough for 99% of plugin functionality, simulating ticking will require a proper impl
    private static class GlobalRegionSchedulerMock implements GlobalRegionScheduler {
        @Override
        public void execute(@NonNull Plugin plugin, @NonNull Runnable run) {
            throw new UnimplementedOperationException("Cannot run tasks with TBPServerMock");
        }

        @Override
        public @NonNull ScheduledTask run(@NonNull Plugin plugin, @NonNull Consumer<ScheduledTask> task) {
            return null;
        }

        @Override
        public @NonNull ScheduledTask runDelayed(@NonNull Plugin plugin, @NonNull Consumer<ScheduledTask> task, long delayTicks) {
            return null;
        }

        @Override
        public @NonNull ScheduledTask runAtFixedRate(@NonNull Plugin plugin, @NonNull Consumer<ScheduledTask> task, long initialDelayTicks, long periodTicks) {
            return null;
        }

        @Override
        public void cancelTasks(@NonNull Plugin plugin) {
            throw new UnimplementedOperationException("Cannot cancel tasks with TBPServerMock");
        }
    }

}
