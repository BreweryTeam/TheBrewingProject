package dev.jsinco.brewery.bukkit.integration.event;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.Pose;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.StopReason;
import dev.jsinco.brewery.api.event.EventProbability;
import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;

public class GSitIntegration implements EventIntegration<GSitIntegration.GSitEvent> {

    private static final String GSIT = "gsit";

    @Override
    public Class<GSitEvent> eClass() {
        return GSitEvent.class;
    }

    @Override
    public Optional<GSitEvent> deserialize(SerializedEvent event) {
        long duration;
        try {
            if (event.meta() == null) {
                duration = 20L;
            } else {
                duration = Long.parseLong(event.meta());
            }
        } catch (IllegalArgumentException ignored) {
            duration = 20L;
        }
        for (PoseType poseType : PoseType.values()) {
            if (event.key().equals(BreweryKey.parse(poseType.name(), GSIT))) {
                return Optional.of(new GSitPoseEvent(poseType, duration));
            }
        }
        if (event.key().equals(BreweryKey.parse("crawl", GSIT))) {
            return Optional.of(new GSitCrawlEvent(duration));
        }
        return Optional.empty();
    }

    @Override
    public SerializedEvent serialize(GSitEvent event) {
        return switch (event) {
            case GSitCrawlEvent(long duration) ->
                    new SerializedEvent(event.key(), String.valueOf(duration));
            case GSitPoseEvent(PoseType ignored, long duration) ->
                    new SerializedEvent(event.key(), String.valueOf(duration));
        };
    }

    @Override
    public String getId() {
        return "gsit";
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("dev.geco.gsit.api.GSitAPI");
    }

    public sealed interface GSitEvent extends IntegrationEvent {

        default void run(Holder.Player player) {
            BukkitAdapter.toPlayer(player)
                    .ifPresent(this::run);
        }

        void run(Player player);
    }

    public record GSitPoseEvent(PoseType poseType, long duration) implements GSitEvent {

        @Override
        public void run(Player player) {
            if (GSitAPI.isPlayerPosing(player)) {
                return;
            }
            Pose pose = GSitAPI.createPose(player.getLocation().getBlock().getRelative(BlockFace.DOWN), player, poseType);
            if (pose == null) {
                return;
            }
            pose.spawn();
            player.getScheduler().runDelayed(TheBrewingProject.getInstance(), ignored -> pose.remove(), () -> {
            }, duration);
        }

        @Override
        public BreweryKey key() {
            return BreweryKey.parse(poseType.name(), "gsit");
        }

        @Override
        public Component displayName() {
            return Component.translatable("tbp.integrations.gsit.pose." + poseType.name().toLowerCase(Locale.ROOT));
        }

        @Override
        public EventProbability probability() {
            return EventProbability.NONE;
        }
    }

    public record GSitCrawlEvent(long duration) implements GSitEvent {

        @Override
        public void run(Player player) {
            if (GSitAPI.isPlayerCrawling(player)) {
                return;
            }
            Crawl crawl = GSitAPI.startCrawl(player);
            if (crawl == null) {
                return;
            }
            player.getScheduler().runDelayed(TheBrewingProject.getInstance(), ignored -> GSitAPI.stopCrawl(crawl, StopReason.PLUGIN), () -> {
            }, duration);
        }

        @Override
        public BreweryKey key() {
            return BreweryKey.parse("crawl", GSIT);
        }

        @Override
        public Component displayName() {
            return Component.translatable("tbp.integrations.gsit.crawl");
        }

        @Override
        public EventProbability probability() {
            return EventProbability.NONE;
        }
    }
}
