package de.skyforce.main.ridecountSystem.storage

import de.skyforce.main.ridecountSystem.model.AttractionKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.UUID

class YamlRidecountStorage(
    private val plugin: JavaPlugin,
    fileName: String = "ridecounts.yml"
) : RidecountStorage {

    private val file: File = File(plugin.dataFolder, fileName)
    private val config: YamlConfiguration

    init {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        config = YamlConfiguration.loadConfiguration(file)
    }

    override fun increment(playerId: UUID, attraction: String): Int {
        val normalizedAttraction = AttractionKey.fromDisplayName(attraction)
        val path = "players.$playerId.$normalizedAttraction"
        val next = config.getInt(path, 0) + 1
        config.set(path, next)
        return next
    }

    override fun save() {
        try {
            config.save(file)
        } catch (ex: IOException) {
            plugin.logger.severe("Konnte Ridecount-Datei nicht speichern: ${file.absolutePath}")
            ex.printStackTrace()
        }
    }

    override fun getPlayerStats(playerId: UUID): Map<String, Int> {
        val section = config.getConfigurationSection("players.$playerId") ?: return emptyMap()
        return section.getKeys(false).associateWith { key ->
            section.getInt(key, 0)
        }
    }

    override fun clearPlayer(playerId: UUID) {
        config.set("players.$playerId", null)
    }

    override fun clearPlayerAttraction(playerId: UUID, attraction: String) {
        val normalizedAttraction = AttractionKey.fromDisplayName(attraction)
        config.set("players.$playerId.$normalizedAttraction", null)
    }
}

