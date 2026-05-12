package de.skyforce.main.ridecountsystem.storage

import de.skyforce.main.ridecountsystem.model.AttractionKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

class YamlRidecountStorage(
    private val file: File,
    private val logger: Logger = Logger.getLogger(YamlRidecountStorage::class.java.name)
) : RidecountStorage {

    private val config: YamlConfiguration
    private var dirty = false

    constructor(plugin: JavaPlugin, fileName: String = "ridecounts.yml") : this(
        file = File(plugin.dataFolder, fileName),
        logger = plugin.logger
    )

    init {
        val parent = file.parentFile
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw IllegalStateException("Konnte Storage-Verzeichnis nicht erstellen: ${parent.absolutePath}")
        }
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw IOException("Datei wurde nicht erstellt.")
                }
            } catch (ex: IOException) {
                logger.log(Level.SEVERE, "Konnte Ridecount-Datei nicht erstellen: ${file.absolutePath}", ex)
                throw IllegalStateException("Ridecount-Storage konnte nicht initialisiert werden.", ex)
            }
        }
        config = YamlConfiguration.loadConfiguration(file)
    }

    override fun increment(playerId: UUID, attraction: String): Int {
        val normalizedAttraction = AttractionKey.fromDisplayName(attraction)
        val path = "players.$playerId.$normalizedAttraction"
        val next = config.getInt(path, 0) + 1
        config.set(path, next)
        dirty = true
        return next
    }

    override fun save(): Boolean {
        if (!dirty) {
            return false
        }

        return try {
            config.save(file)
            dirty = false
            true
        } catch (ex: IOException) {
            logger.log(Level.SEVERE, "Konnte Ridecount-Datei nicht speichern: ${file.absolutePath}", ex)
            false
        }
    }

    override fun getPlayerStats(playerId: UUID): Map<String, Int> {
        val section = config.getConfigurationSection("players.$playerId") ?: return emptyMap()
        return section.getKeys(false).associateWith { key ->
            section.getInt(key, 0)
        }
    }

    override fun getKnownPlayerIds(): Set<UUID> {
        val section = config.getConfigurationSection("players") ?: return emptySet()
        return section.getKeys(false).mapNotNull { raw ->
            try {
                UUID.fromString(raw)
            } catch (_: IllegalArgumentException) {
                logger.warning("Ignoriere ungueltige Spieler-UUID im Ridecount-Storage: $raw")
                null
            }
        }.toSet()
    }

    override fun clearPlayer(playerId: UUID): Boolean {
        if (!config.contains("players.$playerId")) {
            return false
        }
        config.set("players.$playerId", null)
        dirty = true
        return true
    }

    override fun clearPlayerAttraction(playerId: UUID, attraction: String): Boolean {
        val normalizedAttraction = AttractionKey.fromDisplayName(attraction)
        val path = "players.$playerId.$normalizedAttraction"
        if (!config.contains(path)) {
            return false
        }
        config.set(path, null)
        val playerPath = "players.$playerId"
        val playerSection = config.getConfigurationSection(playerPath)
        if (playerSection != null && playerSection.getKeys(false).isEmpty()) {
            config.set(playerPath, null)
        }
        dirty = true
        return true
    }
}

