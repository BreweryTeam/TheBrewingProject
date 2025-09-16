package dev.jsinco.brewery.bukkit;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewManager;
import dev.jsinco.brewery.api.breweries.Barrel;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.Distillery;
import dev.jsinco.brewery.api.breweries.Tickable;
import dev.jsinco.brewery.api.event.CustomEventRegistry;
import dev.jsinco.brewery.api.event.EventStepRegistry;
import dev.jsinco.brewery.api.structure.MultiblockStructure;
import dev.jsinco.brewery.api.structure.StructureMeta;
import dev.jsinco.brewery.api.structure.StructureType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import dev.jsinco.brewery.bukkit.brew.BukkitBrewManager;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import dev.jsinco.brewery.bukkit.command.BreweryCommand;
import dev.jsinco.brewery.bukkit.configuration.serializer.BreweryLocationSerializer;
import dev.jsinco.brewery.bukkit.configuration.serializer.MaterialSerializer;
import dev.jsinco.brewery.bukkit.effect.SqlDrunkStateDataType;
import dev.jsinco.brewery.bukkit.effect.SqlDrunkenModifierDataType;
import dev.jsinco.brewery.bukkit.effect.event.ActiveEventsRegistry;
import dev.jsinco.brewery.bukkit.effect.event.DrunkEventExecutor;
import dev.jsinco.brewery.bukkit.event.*;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.integration.IntegrationManagerImpl;
import dev.jsinco.brewery.bukkit.migration.Migrations;
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResultReader;
import dev.jsinco.brewery.bukkit.recipe.DefaultRecipeReader;
import dev.jsinco.brewery.bukkit.structure.*;
import dev.jsinco.brewery.bukkit.util.BreweryTimeDataType;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.configuration.OkaeriSerdesPackBuilder;
import dev.jsinco.brewery.configuration.locale.BreweryTranslator;
import dev.jsinco.brewery.configuration.serializers.*;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.database.sql.DatabaseDriver;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.effect.text.DrunkTextRegistry;
import dev.jsinco.brewery.format.TimeFormatRegistry;
import dev.jsinco.brewery.recipes.RecipeReader;
import dev.jsinco.brewery.recipes.RecipeRegistryImpl;
import dev.jsinco.brewery.structure.PlacedStructureRegistryImpl;
import dev.jsinco.brewery.util.ClassUtil;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TheBrewingProject extends JavaPlugin implements TheBrewingProjectApi {

    @Getter
    private static TheBrewingProject instance;
    @Getter
    private StructureRegistry structureRegistry;
    @Getter
    private PlacedStructureRegistryImpl placedStructureRegistry;
    @Getter
    private RecipeRegistryImpl<ItemStack> recipeRegistry;
    @Getter
    private BreweryRegistry breweryRegistry;
    @Getter
    private Database database;
    @Getter
    private DrunkTextRegistry drunkTextRegistry;
    @Getter
    private TimeFormatRegistry timeFormatRegistry;
    @Getter
    private DrunksManagerImpl<Connection> drunksManager;
    @Getter
    private CustomEventRegistry customDrunkEventRegistry;
    private WorldEventListener worldEventListener;
    @Getter
    private EventStepRegistry eventStepRegistry;
    @Getter
    private DrunkEventExecutor drunkEventExecutor;
    @Getter
    private long time;
    @Getter
    private BrewManager<ItemStack> brewManager = new BukkitBrewManager();
    @Getter
    private final IntegrationManagerImpl integrationManager = new IntegrationManagerImpl();
    @Getter
    private final ActiveEventsRegistry activeEventsRegistry = new ActiveEventsRegistry();
    @Getter
    private PlayerWalkListener playerWalkListener;
    private BreweryTranslator translator;
    private boolean successfullLoad = false;

    public void initialize() {
        instance = this;
        EventSection.migrateEvents(getDataFolder());
        Config.load(this.getDataFolder(), serializers());
        DrunkenModifierSection.load(this.getDataFolder(), serializers());
        EventSection.load(getDataFolder(), serializers());
        DrunkenModifierSection.validate();
        EventSection.validate();
        this.translator = new BreweryTranslator(new File(this.getDataFolder(), "locale"));
        translator.reload();
        GlobalTranslator.translator().addSource(translator);
        this.structureRegistry = new StructureRegistry();
        this.placedStructureRegistry = new PlacedStructureRegistryImpl();
        this.breweryRegistry = new BreweryRegistry();
        this.recipeRegistry = new RecipeRegistryImpl<>();
        this.drunkTextRegistry = new DrunkTextRegistry();
        this.timeFormatRegistry = new TimeFormatRegistry();
        this.customDrunkEventRegistry = EventSection.events().customEvents();
        this.eventStepRegistry = new EventStepRegistry();
        this.drunkEventExecutor = new DrunkEventExecutor();
    }

    @Override
    public void onLoad() {
        saveResources();
        Migrations.migrateAllConfigFiles(this.getDataFolder());
        initialize();
        Bukkit.getServicesManager().register(TheBrewingProjectApi.class, this, this, ServicePriority.Normal);
        integrationManager.registerIntegrations();
        integrationManager.loadIntegrations();
        this.successfullLoad = true;
    }

    private OkaeriSerdesPack serializers() {
        return new OkaeriSerdesPackBuilder()
                .add(new BreweryLocationSerializer())
                .add(new EventRegistrySerializer())
                .add(new EventStepSerializer())
                .add(new CustomEventSerializer())
                .add(new SoundDefinitionSerializer())
                .add(new IntervalSerializer())
                .add(new MaterialSerializer())
                .add(new LocaleSerializer())
                .add(new ConsumableSerializer())
                .add(new NamedDrunkEventSerializer())
                .add(new DrunkenModifierSerializer())
                .add(new ModifierExpressionSerializer())
                .add(new ModifierDisplaySerializer())
                .add(new ComponentSerializer())
                .add(new ModifierTooltipSerializer())
                .add(new EventProbabilitySerializer())
                .add(new RangeDSerializer())
                .add(new ConditionSerializer())
                .add(new SecretKeySerializer())
                .add(new MinutesDurationSerializer())
                .add(new TicksDurationSerializer())
                .build();
    }

    public void reload() {
        Migrations.migrateAllConfigFiles(this.getDataFolder());
        saveResources();
        closeDatabase();
        Config.config().load(true);
        DrunkenModifierSection.modifiers().load(true);
        EventSection.events().load(true);
        DrunkenModifierSection.validate();
        EventSection.validate();
        translator.reload();
        this.structureRegistry.clear();
        this.placedStructureRegistry.clear();
        this.breweryRegistry.clear();
        loadStructures();
        this.drunkTextRegistry.clear();
        this.customDrunkEventRegistry.clear();
        EventSection.events().customEvents().events()
                .forEach(this.customDrunkEventRegistry::registerCustomEvent);
        this.drunkEventExecutor.clear();
        this.customDrunkEventRegistry = EventSection.events().customEvents();
        saveResources();
        this.database = new Database(DatabaseDriver.SQLITE);
        try {
            database.init(this.getDataFolder());
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e); // Hard exit if any issues here
        }
        this.drunksManager.reset(EventSection.events().enabledRandomEvents().stream().map(BreweryKey::parse).collect(Collectors.toSet()));
        worldEventListener.init();
        RecipeReader<ItemStack> recipeReader = new RecipeReader<>(this.getDataFolder(), new BukkitRecipeResultReader(), BukkitIngredientManager.INSTANCE);

        recipeReader.readRecipes().forEach(recipeFuture -> recipeFuture.thenAcceptAsync(recipe -> recipeRegistry.registerRecipe(recipe)));
        DefaultRecipeReader.readDefaultRecipes(this.getDataFolder()).forEach((string, defaultRecipe) -> defaultRecipe
                .thenAcceptAsync(defaultRecipe1 -> this.recipeRegistry.registerDefaultRecipe(string, defaultRecipe1))
        );
        loadDrunkenReplacements();
        loadTimeFormats();
    }

    private void loadDrunkenReplacements() {
        File file = new File(getDataFolder(), "/locale/" + Config.config().language().toLanguageTag() + ".drunk_text.json");
        if (!file.exists()) {
            Logger.log("Could not find drunken text replacements for your language, using en-US");
            file = new File(getDataFolder(), "/locale/en-US.drunk_text.json");
        }
        try (InputStream inputStream = new FileInputStream(file)) {
            drunkTextRegistry.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTimeFormats() {
        String fileName = Config.config().language().toLanguageTag() + ".time.properties";
        File file = new File(getDataFolder(), "/locale/" + fileName);
        if (!file.exists() && TimeFormatRegistry.class.getResource("/locale/" + fileName) == null) {
            Logger.log("Could not find time formats for your language, using en-US");
            file = new File(getDataFolder(), "/locale/en-US.time.properties");
        }
        try {
            timeFormatRegistry.sync(file);
            timeFormatRegistry.load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadStructures() {
        File structureRoot = new File(getDataFolder(), "structures");
        if (!structureRoot.exists() && !structureRoot.mkdirs()) {
            throw new RuntimeException("Could not create structure root: " + structureRoot);
        }
        Stream.of("small_barrel", "large_barrel", "bamboo_distillery")
                .map(string -> "structures/" + string)
                .flatMap(name -> Stream.of(name + ".schem", name + ".json"))
                .forEach(this::saveResourceIfNotExists);
        Stream.of(structureRoot.listFiles())
                .filter(file -> file.getName().endsWith(".json"))
                .map(File::toPath)
                .filter(StructureJsonFormatValidator::validate)
                .flatMap(path -> {
                    try {
                        return Stream.of(StructureReader.fromJson(path));
                    } catch (IOException | StructureReadException e) {
                        Logger.logErr("Could not load structure: " + path);
                        Logger.logErr(e);
                        return Stream.empty();
                    }
                })
                .forEach(structure -> {
                    if (structure.getMetaOrDefault(StructureMeta.USE_BARREL_SUBSTITUTION, false)) {
                        structureRegistry.addStructure(structure, BarrelBlockDataMatcher.INSTANCE, BarrelType.PLACEABLE_TYPES);
                    } else {
                        structureRegistry.addStructure(structure);
                    }
                });
    }

    @Override
    public void onEnable() {
        Preconditions.checkState(successfullLoad, "Plugin loading failed, see above exception in load stage");
        loadStructures();
        integrationManager.enableIntegrations();
        this.database = new Database(DatabaseDriver.SQLITE);
        try {
            database.init(this.getDataFolder());
            this.time = database.getSingletonNow(BreweryTimeDataType.INSTANCE);
        } catch (IOException | PersistenceException | SQLException e) {
            throw new RuntimeException(e); // Hard exit if any issues here
        }
        this.drunksManager = new DrunksManagerImpl<>(customDrunkEventRegistry, EventSection.events().enabledRandomEvents().stream().map(BreweryKey::parse).collect(Collectors.toSet()),
                () -> this.time, database, SqlDrunkStateDataType.INSTANCE, SqlDrunkenModifierDataType.INSTANCE);
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BlockEventListener(this.structureRegistry, placedStructureRegistry, this.database, this.breweryRegistry), this);
        pluginManager.registerEvents(new PlayerEventListener(this.placedStructureRegistry, this.breweryRegistry, this.database, this.drunksManager, this.drunkTextRegistry, recipeRegistry, drunkEventExecutor), this);
        pluginManager.registerEvents(new InventoryEventListener(breweryRegistry, database), this);
        this.worldEventListener = new WorldEventListener(this.database, this.placedStructureRegistry, this.breweryRegistry);
        worldEventListener.init();
        this.playerWalkListener = new PlayerWalkListener();
        pluginManager.registerEvents(worldEventListener, this);
        pluginManager.registerEvents(playerWalkListener, this);
        pluginManager.registerEvents(new EntityEventListener(), this);
        if (ClassUtil.exists("io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent")) {
            pluginManager.registerEvents(new PlayerJoinListener(), this);
        } else {
            pluginManager.registerEvents(new LegacyPlayerJoinListener(), this);
        }
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, this::updateStructures, 1, 1);
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, this::otherTicking, 1, 1);
        RecipeReader<ItemStack> recipeReader = new RecipeReader<>(this.getDataFolder(), new BukkitRecipeResultReader(), BukkitIngredientManager.INSTANCE);

        recipeReader.readRecipes().forEach(recipeFuture -> recipeFuture.thenAcceptAsync(recipe -> recipeRegistry.registerRecipe(recipe)));
        DefaultRecipeReader.readDefaultRecipes(this.getDataFolder()).forEach((string, defaultRecipe) -> defaultRecipe
                .thenAcceptAsync(defaultRecipe1 -> this.recipeRegistry.registerDefaultRecipe(string, defaultRecipe1))
        );
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, BreweryCommand::register);
        loadDrunkenReplacements();
        loadTimeFormats();
    }

    @Override
    public void onDisable() {
        closeDatabase();
    }

    private void closeDatabase() {
        try {
            breweryRegistry.<BukkitBarrel>getOpened(StructureType.BARREL).forEach(barrel -> barrel.close(true));
            breweryRegistry.<BukkitDistillery>getOpened(StructureType.DISTILLERY).forEach(distillery -> distillery.close(true));
        } catch (Throwable e) {
            Logger.logErr(e);
        }
        try {
            database.setSingleton(BreweryTimeDataType.INSTANCE, time).join();
            database.flush().join();
        } catch (PersistenceException e) {
            Logger.logErr(e);
        }
    }

    private void saveResources() {
        Stream.of("recipes.yml", "locale/en-US.drunk_text.json", "locale/ru.drunk_text.json")
                .forEach(this::saveResourceIfNotExists);
    }

    private void saveResourceIfNotExists(String resource) {
        if (new File(getDataFolder(), resource).exists()) {
            return;
        }
        super.saveResource(resource, false);
    }

    private void updateStructures(ScheduledTask ignored) {
        breweryRegistry.getActiveSinglePositionStructure().stream()
                .filter(Tickable.class::isInstance)
                .map(Tickable.class::cast)
                .forEach(Tickable::tick);
        placedStructureRegistry.getStructures(StructureType.DISTILLERY).stream()
                .map(MultiblockStructure::getHolder)
                .map(Distillery.class::cast)
                .forEach(Distillery::tick);
        List.copyOf(breweryRegistry.<BukkitBarrel>getOpened(StructureType.BARREL)).forEach(Barrel::tickInventory);
        List.copyOf(breweryRegistry.<BukkitDistillery>getOpened(StructureType.DISTILLERY)).forEach(Distillery::tickInventory);
    }

    private void otherTicking(ScheduledTask ignored) {
        drunksManager.tick(drunkEventExecutor::doDrunkEvent, uuid -> Bukkit.getPlayer(uuid) != null);
        try {
            if (++time % 200 == 0) {
                database.setSingleton(BreweryTimeDataType.INSTANCE, time);
            }
        } catch (PersistenceException e) {
            Logger.logErr(e);
        }
    }

    public static NamespacedKey key(String key) {
        return new NamespacedKey("brewery", key);
    }
}

