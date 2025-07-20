package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.ExecutableEventStep;
import dev.jsinco.brewery.event.step.WaitStep;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

public class WaitStepImpl extends WaitStep implements ExecutableEventStep {

    public WaitStepImpl(int durationTicks) {
        super(durationTicks);
    }

    public WaitStepImpl(String duration) {
        super(duration);
    }

    @Override
    public void execute(UUID contextPlayer, List<EventStep> events, int index) {
        if (index + 1 >= events.size()) {
            return;
        }

        final List<EventStep> eventsLeft = events.subList(index + 1, events.size());
        Bukkit.getScheduler().runTaskLater(
                TheBrewingProject.getInstance(),
                () -> TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvents(contextPlayer, eventsLeft),
                getDurationTicks()
        );
    }

}
