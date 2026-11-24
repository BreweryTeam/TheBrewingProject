package dev.jsinco.brewery.integration.hook.auraskills;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.skill.CustomSkill;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.LevelerContext;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.SourceType;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewManager;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.recipe.RecipeMatcher;
import dev.jsinco.brewery.api.recipe.RecipeMatcherResult;
import dev.jsinco.brewery.api.util.ClassUtil;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryExtractEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuraSkillsIntegration implements Integration, Listener {

    private static final CustomSkill BREWING = CustomSkill.builder(NamespacedId.of("tbp", "brewing"))
            .item(ItemContext.builder()
                    .material("cauldron")
                    .pos("4,4")
                    .build()
            ).displayName("Brewing")
            .description("Brew beverages")
            .build();
    private static final Key TAGGED = TheBrewingProjectApi.key("aura_skills/tagged");

    private final RecipeMatcher<ItemStack> matcher;
    private final BrewManager<ItemStack> brewManager;
    private final File workFolder;
    private final Plugin owner;

    private SourceType cook;
    private SourceType mix;
    private SourceType distill;
    private SourceType age;

    public AuraSkillsIntegration(Plugin owner, TheBrewingProjectApi tbpApi) {
        this.workFolder = new File(owner.getDataFolder(), "auraskills");
        this.owner = owner;
        this.matcher = tbpApi.getBrewManager().matcher()
                .build();
        this.brewManager = tbpApi.getBrewManager();
    }

    @Override
    public void onEnable() {
        writeResource("sources/brewing.yml");
        writeResource("/brewing.yml");
        AuraSkillsApi api = AuraSkillsApi.get();
        NamespacedRegistry registry = api.useRegistry("tbp", workFolder);
        registry.registerSkill(BREWING);
        this.cook = registry.registerSourceType("cook", BrewSourceSettings::from);
        this.mix = registry.registerSourceType("mix", BrewSourceSettings::from);
        this.distill = registry.registerSourceType("distill", BrewSourceSettings::from);
        this.age = registry.registerSourceType("age", BrewSourceSettings::from);
        Bukkit.getPluginManager().registerEvents(this, owner);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCauldronBrew(CauldronExtractEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        List<SkillSource<BrewSourceSettings>> sourceSettings = AuraSkillsApi.get().getSourceManager()
                .getSourcesOfType(BrewSourceSettings.class);
        Brew brew = event.getCauldron().getBrew();
        List<SkillSource<BrewSourceSettings>> skills = null;
        LevelerContext context = null;
        if (brew.lastCompletedStep() instanceof BrewingStep.Mix) {
            skills = sourceSettings.stream()
                    .filter(source -> source.source().getId().getKey().equals(mix.getId().getKey()))
                    .toList();
            context = new LevelerContext(AuraSkillsApi.get(), mix);
        } else if (brew.lastCompletedStep() instanceof BrewingStep.Cook) {
            skills = sourceSettings.stream()
                    .filter(source -> source.source().getId().getKey().equals(cook.getId().getKey()))
                    .toList();
            context = new LevelerContext(AuraSkillsApi.get(), cook);
        }
        if (skills == null) {
            return;
        }
        ItemStack result = event.getItemResult().get();
        if (actOnBrew(brew, context, player, result, skills)) {
            event.setItemResult(result);
        }
    }

    private void tag(ItemStack itemBrew, Skill skill) {
        MetaData previousMeta = brewManager.brewMeta(itemBrew)
                .orElse(new MetaData());
        List<String> strings = previousMeta.meta(TAGGED, MetaDataType.STRING_LIST);
        List<String> toWrite = strings == null ? new ArrayList<>() : new ArrayList<>(strings);
        toWrite.add(skill.getId().toString());
        brewManager.setMeta(itemBrew, previousMeta.withMeta(TAGGED, MetaDataType.STRING_LIST, toWrite));
    }

    private boolean alreadyTagged(Brew brew, Skill skill) {
        List<String> tagged = brew.meta(TAGGED, MetaDataType.STRING_LIST);
        return tagged != null && tagged.contains(skill.getId().toString());
    }

    @EventHandler(ignoreCancelled = true)
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
        List<SkillSource<BrewSourceSettings>> skills = sourceSettings.stream()
                .filter(source -> source.source().getId().getKey().equals(distill.getId().getKey()))
                .toList();
        LevelerContext context = new LevelerContext(AuraSkillsApi.get(), distill);
        ItemStack result = event.getTransactionSession().getResult().itemStack();
        if (actOnBrew(brew, context, player, result, skills)) {
            event.getTransactionSession().setResult(new ItemSource.ItemBasedSource(result));
        }
    }

    @EventHandler(ignoreCancelled = true)
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
        List<SkillSource<BrewSourceSettings>> skills = sourceSettings.stream()
                .filter(source -> source.source().getId().getKey().equals(age.getId().getKey()))
                .toList();
        LevelerContext context = new LevelerContext(AuraSkillsApi.get(), distill);
        ItemStack result = event.getTransactionSession().getResult().itemStack();
        if (actOnBrew(brew, context, player, result, skills)) {
            event.getTransactionSession().setResult(new ItemSource.ItemBasedSource(result));
        }
    }

    private boolean actOnBrew(Brew brew, LevelerContext context, Player player, ItemStack result, List<SkillSource<BrewSourceSettings>> skills) {
        boolean match = false;
        for (SkillSource<BrewSourceSettings> source : skills) {
            if (!matches(brew, source.source().mode())
                    || context.failsChecks(player, player.getLocation(), source.skill())
                    || alreadyTagged(brew, source.skill())
            ) {
                continue;
            }
            match = true;
            AuraSkillsApi.get().getUserManager().getUser(player.getUniqueId())
                    .addSkillXp(source.skill(), source.source().getXp(), source.source());
            tag(result, source.skill());
        }
        return match;
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

    public void writeResource(String resourcePath) {
        File target = new File(workFolder, resourcePath);
        if (target.exists()) {
            return;
        }
        try {
            if (!target.getParentFile().mkdirs() && !target.createNewFile()) {
                Logger.logErr("Could not create file '%s', probably permission issue".formatted(target));
                return;
            }
            try (InputStream inputStream = AuraSkillsIntegration.class.getResourceAsStream("/auraskills/%s".formatted(resourcePath))) {
                if (inputStream == null) {
                    Logger.logErr("Internal resource does not exist: /auraskills/%s".formatted(resourcePath));
                    return;
                }
                try (OutputStream outputStream = new FileOutputStream(target)) {
                    inputStream.transferTo(outputStream);
                }
            }
        } catch (IOException e) {
            Logger.logErr("Could not write config file: %s".formatted(target));
        }
    }

    @Override
    public String getId() {
        return "aura_skills";
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("dev.aurelium.auraskills.api.AuraSkillsApi");
    }
}
