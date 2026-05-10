package de.skyforce.main.ridecountSystem.config

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PluginConfig(plugin: JavaPlugin) {

    private val file: File = File(plugin.dataFolder, "config.yml")
    private val config: YamlConfiguration

    init {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        if (!file.exists()) {
            createDefaultConfig()
            config = YamlConfiguration.loadConfiguration(file)
        } else {
            config = YamlConfiguration.loadConfiguration(file)
        }
    }

    private fun createDefaultConfig() {
        val defaults = YamlConfiguration()
        defaults.set("ridecount.sign-duplicate-cooldown-ms", 2000L)
        defaults.set("ridecount.enable-debug-logging", false)
        defaults.set("storage.auto-save-interval-minutes", 5)
        defaults.set("storage.file-name", "ridecounts.yml")

        try {
            defaults.save(file)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun getSignDuplicateCooldownMs(): Long {
        return config.getLong("ridecount.sign-duplicate-cooldown-ms", 2000L)
    }

    fun isDebugLoggingEnabled(): Boolean {
        return config.getBoolean("ridecount.enable-debug-logging", false)
    }

    fun getAutoSaveIntervalMinutes(): Int {
        return config.getInt("storage.auto-save-interval-minutes", 5)
    }

    fun getStorageFileName(): String {
        return config.getString("storage.file-name", "ridecounts.yml")!!
    }
}

