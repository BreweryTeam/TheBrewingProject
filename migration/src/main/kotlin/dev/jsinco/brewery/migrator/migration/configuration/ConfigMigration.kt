package dev.jsinco.brewery.migrator.migration.configuration

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File

object ConfigMigration {

    fun migrateConfig(breweryXFolder: File, tbpFolder: File) {
        val breweryXConfigFile = File(breweryXFolder, "config.yml")
        val tbpConfigFile = File(tbpFolder, "config.yml")

        require(breweryXConfigFile.exists()) { "BreweryX config.yml not found at ${breweryXConfigFile.absolutePath}" }
        require(tbpConfigFile.exists()) { "TBP config.yml not found at ${tbpConfigFile.absolutePath}" }

        val yaml = Yaml(DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
        })

        val breweryConfig = (yaml.load<Map<String, Any?>>(breweryXConfigFile.readText())?.toMutableMap())
            ?: mutableMapOf()
        val tbpConfig = (yaml.load<Map<String, Any?>>(tbpConfigFile.readText())?.toMutableMap())
            ?: mutableMapOf()

        val encodeKey = (breweryConfig["encodeKey"] as? Number)?.toLong()
            ?: error("encodeKey not found or not a number in ${breweryXConfigFile.absolutePath}")
        val seeds: MutableList<Long> = when (val existingSeeds = tbpConfig["breweryxMigrationSeeds"]) {
            null -> mutableListOf()
            is List<*> -> existingSeeds.mapNotNull { (it as? Number)?.toLong() }.toMutableList()
            else -> error("breweryxMigrationSeeds exists but is not a list in ${tbpConfigFile.absolutePath}")
        }
        if (!seeds.contains(encodeKey)) seeds.add(encodeKey)
        tbpConfig["breweryxMigrationSeeds"] = seeds
        tbpConfigFile.writeText(yaml.dump(tbpConfig))
    }

}