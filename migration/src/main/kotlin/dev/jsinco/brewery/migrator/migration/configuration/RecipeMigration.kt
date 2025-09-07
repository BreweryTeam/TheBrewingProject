package dev.jsinco.brewery.migrator.migration.configuration

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.regex.Pattern
import java.util.stream.Collectors

object RecipeMigration {

    val KEY_REMAPPING = mapOf(
        "enabled" to null,
        "name" to "potion-attributes.name",
        "ingredients" to null,
        "cookingtime" to null,
        "distillruns" to null,
        "distilltime" to null,
        "wood" to null,
        "age" to null,
        "color" to "potion-attributes.color",
        "difficulty" to "brew-difficulty",
        "alcohol" to "modifiers.alcohol",
        "lore" to "potion-attributes.lore",
        "servercommands" to null,
        "playercommands" to null,
        "drinkmessage" to "messages.message",
        "drinktitle" to "messages.title",
        "glint" to "potion-attributes.glint",
        "customModelData" to "potion-attributes.custom-model-data",
        "effects" to "effects"
    )

    val INGREDIENT_REPLACEMENTS = mapOf(
        "grass" to "short_grass", "blue-flowers" to "blue_orchid"
    )

    val BARREL_TYPE_REPLACEMENTS = (sequenceOf(
        "any",
        "birch",
        "oak",
        "jungle",
        "acacia",
        "dark_oak",
        "crimson",
        "warped",
        "mangrove",
        "cherry",
        "bamboo",
        "copper",
        "pale_oak"
    ).mapIndexed { index, string ->
        index.toString() to string
    } + sequenceOf("cut_copper" to "copper")).toMap()

    fun migrateRecipes(breweryXFolder: File, tbpFolder: File) {
        val breweryXConfiguration = YamlConfiguration.loadConfiguration(File(breweryXFolder, "recipes.yml"))
        val recipesConfiguration = breweryXConfiguration.getConfigurationSection("recipes")!!
        val tbpRecipesFile = File(tbpFolder, "recipes.yml")
        val tbpEventsFile = File(tbpFolder, "events.yml")
        val tbpRecipes = YamlConfiguration.loadConfiguration(tbpRecipesFile)
        val tbpEvents = YamlConfiguration.loadConfiguration(tbpEventsFile)
        for (key in recipesConfiguration.getKeys(false)) {
            if (key == "ex") {
                continue
            }
            val recipeConfiguration = recipesConfiguration.getConfigurationSection(key)!!
            for (recipeKey in recipeConfiguration.getKeys(true)) {
                val newKey = KEY_REMAPPING[recipeKey]
                newKey?.let {
                    val newValue = processValue(recipeKey, recipeConfiguration.get(recipeKey)!!)
                    tbpRecipes.set("recipes.${key}.${newKey}", newValue)
                }
            }
            tbpRecipes.set("recipes.${key}.steps", compileSteps(recipeConfiguration))
            if (recipeConfiguration.contains("servercommands") || recipeConfiguration.contains("playercommands")) {
                val serverCommands = recipeConfiguration.getStringList("servercommands")
                val playerCommands = recipeConfiguration.getStringList("playercommands")
                val qualities = listOf("+", "++", "+++", "").filter { quality ->
                    serverCommands.asSequence().any {
                        it.startsWith("$quality ") || (quality.isEmpty() && !it.contains("^\\++ ".toRegex()))
                    } || playerCommands.asSequence()
                        .any {
                            it.startsWith("$quality ") || (quality.isEmpty() && !it.contains("^\\++ ".toRegex()))
                        }
                }
                val qualityNames = mapOf(
                    "+" to "poor",
                    "++" to "good",
                    "+++" to "excellent",
                    "" to "generic"
                )
                qualities
                    .forEach {
                        tbpEvents.set(
                            "custom-events.${qualityNames[it]!!}_${key}_event.steps",
                            compileEvents(recipeConfiguration, it)
                        )
                    }
                tbpRecipes.set("recipes.${key}.events", qualities.map { "$it${qualityNames[it]!!}_${key}_event" })
            }
        }
        tbpRecipes.save(tbpRecipesFile)
        tbpEvents.save(tbpEventsFile)
    }

    private fun compileEvents(recipeConfiguration: ConfigurationSection, quality: String): List<Map<String, Any>> {
        return (recipeConfiguration.getStringList("servercommands").asSequence()
            .filter {
                it.startsWith("$quality ") || (quality.isEmpty() && !it.contains("^\\++ ".toRegex()))
            }.map {
                it.replace("^\\++ ".toRegex(), "")
            }.map {
                mapOf(
                    "command" to it
                )
            } + recipeConfiguration.getStringList("playercommands")
            .filter {
                it.startsWith("$quality ") || (quality.isEmpty() && !it.contains("^\\++ ".toRegex()))
            }.map {
                it.replace("^\\++ ".toRegex(), "")
            }.map {
                mapOf(
                    "command" to it, "as" to "player"
                )
            }).toList()
    }

    private fun compileSteps(recipeConfiguration: ConfigurationSection): List<Map<String, Any>> {
        val output: MutableList<Map<String, Any>> = mutableListOf()
        val cookingTime = recipeConfiguration.getInt("cookingtime")
        val ingredients = recipeConfiguration.getStringList("ingredients")
        output.add(
            mapOf(
                "type" to "cook",
                "cauldron-type" to "water",
                "cook-time" to cookingTime,
                "ingredients" to processValue("ingredients", ingredients)
            )
        )
        val distillRuns = recipeConfiguration.getInt("distillruns", 0)
        if (distillRuns > 0) {
            output.add(
                mapOf(
                    "type" to "distill", "runs" to distillRuns
                )
            )
        }
        val ageingTime = recipeConfiguration.getInt("age", 0)
        val barrelType = recipeConfiguration.getString("wood") ?: "any"
        if (ageingTime > 0) {
            val revisedBarrelType =
                BARREL_TYPE_REPLACEMENTS.keys.asSequence().filter { barrelType.contains(it, ignoreCase = true) }
                    .map { barrelType.replace(it, BARREL_TYPE_REPLACEMENTS[it]!!, ignoreCase = true) }.firstOrNull()
                    ?: barrelType
            output.add(
                mapOf(
                    "type" to "age", "age-years" to ageingTime, "barrel-type" to revisedBarrelType
                )
            )
        }
        return output
    }

    fun processValue(key: String, value: Any): Any {
        if (value is List<*>) {
            if (key == "effects") {
                return value.asSequence().flatMap { effectSpecification ->
                    if (effectSpecification !is String) {
                        return@flatMap emptySequence()
                    }
                    val split = effectSpecification.split("/")
                    val potionEffectName = split.first().lowercase()
                    val amplifierString = split.getOrNull(1) ?: "1"
                    val durationString = split.getOrNull(2) ?: "1"
                    if (!amplifierString.contains("-") && !durationString.contains("-")) {
                        return@flatMap sequenceOf("${potionEffectName}/${amplifierString}/${durationString}")
                    }
                    val amplifier = breweryXRange(amplifierString)
                    val duration = breweryXRange(durationString)
                    val fixedAmplifier = fixRange(amplifier)
                    val fixedDuration = fixRange(duration)
                    return@flatMap sequenceOf(
                        "+${potionEffectName}/${amplifier.first}/${duration.first}",
                        "++${potionEffectName}/${fixedAmplifier}/${fixedDuration}",
                        "+++${potionEffectName}/${amplifier.second}/${duration.second}"
                    )
                }.toList()
            }
            return value.asSequence().mapNotNull { it }.map { processValue(key, it) }.toList()
        }
        if (value is String) {
            if (key == "ingredients") {
                return INGREDIENT_REPLACEMENTS.keys.asSequence().filter { value.contains(it, ignoreCase = true) }
                    .map { value.replace(it, INGREDIENT_REPLACEMENTS[it]!!, ignoreCase = true) }.firstOrNull()
                    ?: value.lowercase()
            }
            val pattern = Pattern.compile("^(\\++) ")
            val matcher = pattern.matcher(value)
            val newValue: String = if (matcher.find()) {
                matcher.group(1) + matcher.replaceAll("")
            } else {
                value
            }
            return newValue.split("/").stream().map {
                MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(it))
            }.collect(Collectors.joining("/"))
        }
        return value
    }

    private fun fixRange(range: Pair<Int, Int>): String {
        val max = range.first.coerceAtLeast(range.second)
        val min = range.first.coerceAtMost(range.second)
        return "${min}-${max}"
    }

    fun breweryXRange(input: String): Pair<Int, Int> {
        val split = input.split("-")
        if (split.size == 1) {
            val value = split.first().toInt()
            return Pair(value, value)
        }
        return Pair(split.first().toInt(), split[1].toInt())
    }
}