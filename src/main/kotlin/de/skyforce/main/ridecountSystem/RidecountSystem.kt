package de.skyforce.main.ridecountSystem

import de.skyforce.main.ridecountSystem.command.RidecountCommand
import de.skyforce.main.ridecountSystem.config.PluginConfig
import de.skyforce.main.ridecountSystem.integration.TrainCartsApiBridge
import de.skyforce.main.ridecountSystem.listener.RidecountSignListener
import de.skyforce.main.ridecountSystem.service.RidecountService
import de.skyforce.main.ridecountSystem.service.RidecountTriggerCooldown
import de.skyforce.main.ridecountSystem.storage.YamlRidecountStorage
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class RidecountSystem : JavaPlugin() {

    private lateinit var storage: YamlRidecountStorage
    private lateinit var config: PluginConfig
    private var autoSaveTask: BukkitTask? = null

    override fun onEnable() {
        config = PluginConfig(this)
        storage = YamlRidecountStorage(this, config.getStorageFileName())
        val service = RidecountService(storage, logger)
        val triggerCooldown = RidecountTriggerCooldown(config.getSignDuplicateCooldownMs())

        val listener = RidecountSignListener(
            plugin = this,
            ridecountService = service,
            triggerCooldown = triggerCooldown,
            handleVehicleMove = true
        )
        server.pluginManager.registerEvents(listener, this)

        val trainCartsBridge = TrainCartsApiBridge(
            plugin = this,
            ridecountService = service,
            triggerCooldown = triggerCooldown
        )
        val nativeTrainCartsBridgeActive = trainCartsBridge.init()

        val cmd = RidecountCommand(storage)
        getCommand("ridecount")?.setExecutor(cmd)
        getCommand("ridecount")?.tabCompleter = cmd

        scheduleAutoSave()

        logger.info("Ridecount-System v${pluginMeta.version} aktiviert.")

        if (nativeTrainCartsBridgeActive) {
            logger.info("TrainCarts API-Bridge aktiv; Bukkit-Fallback bleibt als Reserve aktiv.")
        } else {
            logger.info("TrainCarts API-Bridge nicht aktiv; Bukkit-Fallback verwendet VehicleMoveEvent.")
        }

        if (config.isDebugLoggingEnabled()) {
            logger.info("Debug-Logging ist AKTIVIERT.")
        }
    }

    override fun onDisable() {
        autoSaveTask?.cancel()
        if (::storage.isInitialized) {
            storage.save()
        }
        logger.info("Ridecount-System deaktiviert.")
    }

    private fun scheduleAutoSave() {
        val intervalMinutes = config.getAutoSaveIntervalMinutes()
        if (intervalMinutes <= 0) {
            return
        }

        val intervalTicks = intervalMinutes * 60L * 20L
        autoSaveTask = server.scheduler.runTaskTimer(
            this,
            Runnable { storage.save() },
            intervalTicks,
            intervalTicks
        )
    }
}
