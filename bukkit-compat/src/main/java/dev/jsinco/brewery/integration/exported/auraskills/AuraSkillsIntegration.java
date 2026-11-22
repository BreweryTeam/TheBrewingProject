package dev.jsinco.brewery.integration.exported.auraskills;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.skill.CustomSkill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.SourceType;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewManager;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.recipe.RecipeMatcher;
import dev.jsinco.brewery.api.recipe.RecipeMatcherResult;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryExtractEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;

public class AuraSkillsIntegration implements Listener {

    private static final CustomSkill BREW = CustomSkill.builder(NamespacedId.of("tbp", "cook"))
            .item(ItemContext.builder()
                    .material("cauldron")
                    .build()
            ).displayName("Cooking beverages")
            .description("Cook beverages")
            .build();

    private final RecipeMatcher<ItemStack> matcher;
    private final BrewManager<ItemStack> brewManager;

    private final SourceType cook;
    private final SourceType mix;
    private final SourceType distill;
    private final SourceType age;

    public AuraSkillsIntegration(Plugin owner, TheBrewingProjectApi tbpApi) {
        this.matcher = tbpApi.getBrewManager().matcher()
                .build();
        this.brewManager = tbpApi.getBrewManager();
        AuraSkillsApi api = AuraSkillsApi.get();
        NamespacedRegistry registry = api.useRegistry("tbp", owner.getDataFolder());
        registry.registerSkill(BREW);
        this.cook = registry.registerSourceType("cook", BrewSourceSettings::from);
        this.mix = registry.registerSourceType("mix", BrewSourceSettings::from);
        this.distill = registry.registerSourceType("distill", BrewSourceSettings::from);
        this.age = registry.registerSourceType("age", BrewSourceSettings::from);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCauldronBrew(CauldronExtractEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        List<SkillSource<BrewSourceSettings>> sourceSettings = AuraSkillsApi.get().getSourceManager()
                .getSourcesOfType(BrewSourceSettings.class);
        Brew brew = event.getCauldron().getBrew();
        SkillSource<BrewSourceSettings> skill = null;
        if (brew.lastCompletedStep() instanceof BrewingStep.Mix) {
            skill = sourceSettings.stream()
                    .filter(source -> source.source().getId().equals(mix.getId()))
                    .findFirst()
                    .orElse(null);

        } else if (brew.lastCompletedStep() instanceof BrewingStep.Cook) {
            skill = sourceSettings.stream()
                    .filter(source -> source.source().getId().equals(cook.getId()))
                    .findFirst()
                    .orElse(null);
        }
        if (skill == null || !matches(brew, skill.source().mode())) {
            return;
        }
        AuraSkillsApi.get().getUserManager().getUser(player.getUniqueId())
                .addSkillXp(BREW, skill.source().multiplier(), skill.source());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDistilleryExtract(DistilleryExtractEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        ItemSource.ItemBasedSource itemSource = event.getTransactionSession().getResult();
        Optional<Brew> brewOptional = itemSource == null ?
                Optional.empty() : brewManager.fromItem(itemSource.itemStack());
        if (brewOptional.isEmpty()) {
            return;
        }
        Brew brew = brewOptional.get();
        List<SkillSource<BrewSourceSettings>> sourceSettings = AuraSkillsApi.get().getSourceManager()
                .getSourcesOfType(BrewSourceSettings.class);
        SkillSource<BrewSourceSettings> skill = sourceSettings.stream()
                .filter(source -> source.source().getId().equals(distill.getId()))
                .findFirst()
                .orElse(null);
        if (skill == null || matches(brew, skill.source().mode())) {
            return;
        }
        AuraSkillsApi.get().getUserManager().getUser(player.getUniqueId())
                .addSkillXp(BREW, skill.source().multiplier(), skill.source());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBarrelExtract(BarrelExtractEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        ItemSource.ItemBasedSource itemSource = event.getTransactionSession().getResult();
        Optional<Brew> brewOptional = itemSource == null ?
                Optional.empty() : brewManager.fromItem(itemSource.itemStack());
        if (brewOptional.isEmpty()) {
            return;
        }
        Brew brew = brewOptional.get();
        List<SkillSource<BrewSourceSettings>> sourceSettings = AuraSkillsApi.get().getSourceManager()
                .getSourcesOfType(BrewSourceSettings.class);
        SkillSource<BrewSourceSettings> skill = sourceSettings.stream()
                .filter(source -> source.source().getId().equals(age.getId()))
                .findFirst()
                .orElse(null);
        if (skill == null || matches(brew, skill.source().mode())) {
            return;
        }
        AuraSkillsApi.get().getUserManager().getUser(player.getUniqueId())
                .addSkillXp(BREW, skill.source().multiplier(), skill.source());
    }

    private boolean matches(Brew brew, CompletionMode mode) {
        if (mode == CompletionMode.ANY) {
            return true;
        }
        RecipeMatcherResult<ItemStack> result = matcher.match(brew);
        BrewScore score = result.score();
        if (score.brewQuality() == null) {
            return false;
        }
        return mode != CompletionMode.COMPLETED || score.completed();
    }
}
