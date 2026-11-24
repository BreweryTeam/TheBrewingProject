package dev.jsinco.brewery.integration.event;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.api.event.PreEntityStopSitEvent;
import dev.geco.gsit.api.event.PrePlayerStopCrawlEvent;
import dev.geco.gsit.api.event.PrePlayerStopPoseEvent;
import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.Pose;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.StopReason;
import dev.jsinco.brewery.api.event.EventData;
import dev.jsinco.brewery.api.event.EventProbability;
import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.ClassUtil;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public record GSitIntegration(Plugin owner) implements EventIntegration<GSitIntegration.GSitEvent>, Listener {

    private static final String GSIT = "gsit";

    @Override
    public Class<GSitEvent> eClass() {
        return GSitEvent.class;
    }

    @Override
    public List<BreweryKey> listEventKeys() {
        List<BreweryKey> output = new ArrayList<>();
        for (PoseType poseType : PoseType.values()) {
            output.add(BreweryKey.parse(poseType.name(), GSIT));
        }
        output.add(BreweryKey.parse("crawl", GSIT));
        output.add(BreweryKey.parse("sit", GSIT));
        return output;
    }

    @Override
    public Optional<GSitEvent> convertToEvent(EventData event) {
        long duration;
        try {
            Long temp = event.data(EventData.DURATION_KEY, MetaDataType.STRING_TO_LONG);
            duration = temp == null ? 20L : temp;
        } catch (Exception e) {
            duration = 20L;
        }
        for (PoseType poseType : PoseType.values()) {
            if (event.key().equals(BreweryKey.parse(poseType.name(), GSIT))) {
                return Optional.of(new GSitPoseEvent(poseType, duration, owner));
            }
        }
        if (event.key().equals(BreweryKey.parse("crawl", GSIT))) {
            return Optional.of(new GSitCrawlEvent(duration, owner));
        }
        if (event.key().equals(BreweryKey.parse("sit", GSIT))) {
            return Optional.of(new GSitSitEvent(duration, owner));
        }
        return Optional.empty();
    }

    @Override
    public EventData convertToData(GSitEvent event) {
        return switch (event) {
            case GSitCrawlEvent(long duration, Plugin ignored) -> new EventData(event.key())
                    .withData(EventData.DURATION_KEY, MetaDataType.STRING_TO_LONG, duration);
            case GSitPoseEvent(PoseType ignored1, long duration, Plugin ignored2) -> new EventData(event.key())
                    .withData(EventData.DURATION_KEY, MetaDataType.STRING_TO_LONG, duration);
            case GSitSitEvent(long duration, Plugin ignored) -> new EventData(event.key())
                    .withData(EventData.DURATION_KEY, MetaDataType.STRING_TO_LONG, duration);
        };
    }

    @Override
    public String getId() {
        return GSIT;
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("dev.geco.gsit.api.GSitAPI");
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, owner);
    }

    @EventHandler
    public void onPreStopPose(PrePlayerStopPoseEvent stopPoseEvent) {
        if (stopPoseEvent.getReason() == StopReason.GET_UP && GSitPoseEvent.active.contains(stopPoseEvent.getPose())) {
            stopPoseEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onPreStopPose(PrePlayerStopCrawlEvent stopCrawlEvent) {
        if (stopCrawlEvent.getReason() == StopReason.GET_UP && GSitCrawlEvent.active.contains(stopCrawlEvent.getCrawl())) {
            stopCrawlEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onPreStopSit(PreEntityStopSitEvent stopSitEvent) {
        if (stopSitEvent.getReason() == StopReason.GET_UP && GSitSitEvent.active.contains(stopSitEvent.getSeat())) {
            stopSitEvent.setCancelled(true);
        }
    }

    public sealed interface GSitEvent extends IntegrationEvent {

        default void run(Holder.Player player) {
            BukkitAdapter.toPlayer(player)
                    .ifPresent(this::run);
        }

        void run(Player player);
    }

    public record GSitPoseEvent(PoseType poseType, long duration, Plugin owner) implements GSitEvent {

        private static final Set<Pose> active = new HashSet<>();

        @Override
        public void run(Player player) {
            if (GSitAPI.isPlayerPosing(player)) {
                return;
            }
            Pose pose = GSitAPI.createPose(player.getLocation().getBlock().getRelative(BlockFace.DOWN), player, poseType);
            if (pose == null) {
                return;
            }
            active.add(pose);
            player.getScheduler().runDelayed(owner, ignored -> {
                GSitAPI.removePose(pose, StopReason.PLUGIN);
                active.remove(pose);
            }, () -> active.remove(pose), duration);
        }

        @Override
        public BreweryKey key() {
            return BreweryKey.parse(poseType.name(), "gsit");
        }

        @Override
        public Component displayName() {
            return Component.translatable("tbp.integration.gsit.pose." + poseType.name().toLowerCase(Locale.ROOT));
        }

        @Override
        public EventProbability probability() {
            return EventProbability.NONE;
        }
    }

    public record GSitCrawlEvent(long duration, Plugin owner) implements GSitEvent {

        private static final Set<Crawl> active = new HashSet<>();

        @Override
        public void run(Player player) {
            if (GSitAPI.isPlayerCrawling(player)) {
                return;
            }
            Crawl crawl = GSitAPI.startCrawl(player);
            if (crawl == null) {
                return;
            }
            active.add(crawl);
            player.getScheduler().runDelayed(owner, ignored -> {
                GSitAPI.stopCrawl(crawl, StopReason.PLUGIN);
                active.remove(crawl);
            }, () -> active.remove(crawl), duration);
        }

        @Override
        public BreweryKey key() {
            return BreweryKey.parse("crawl", GSIT);
        }

        @Override
        public Component displayName() {
            return Component.translatable("tbp.integration.gsit.crawl");
        }

        @Override
        public EventProbability probability() {
            return EventProbability.NONE;
        }
    }

    public record GSitSitEvent(long duration, Plugin owner) implements GSitEvent {

        private static final Set<Seat> active = new HashSet<>();

        @Override
        public void run(Player player) {
            if (GSitAPI.isEntitySitting(player)) {
                return;
            }
            Seat seat = GSitAPI.createSeat(player.getLocation().getBlock().getRelative(BlockFace.DOWN), player);
            if (seat == null) {
                return;
            }
            active.add(seat);
            player.getScheduler().runDelayed(owner, ignored -> {
                active.remove(seat);
                GSitAPI.removeSeat(seat, StopReason.PLUGIN);
            }, () -> active.remove(seat), duration);
        }

        @Override
        public BreweryKey key() {
            return BreweryKey.parse("sit", GSIT);
        }

        @Override
        public Component displayName() {
            return Component.translatable("tbp.integration.gsit.sit");
        }

        @Override
        public EventProbability probability() {
            return EventProbability.NONE;
        }
    }
}
