package de.skyforce.main.ridecountSystem

import de.skyforce.main.ridecountSystem.command.RidecountCommand
import de.skyforce.main.ridecountSystem.config.PluginConfig
import de.skyforce.main.ridecountSystem.listener.RidecountSignListener
import de.skyforce.main.ridecountSystem.service.RidecountService
import de.skyforce.main.ridecountSystem.storage.YamlRidecountStorage
import org.bukkit.plugin.java.JavaPlugin

class RidecountSystem : JavaPlugin() {

    private lateinit var storage: YamlRidecountStorage
    private lateinit var config: PluginConfig

    override fun onEnable() {
        config = PluginConfig(this)
        storage = YamlRidecountStorage(this, config.getStorageFileName())
        val service = RidecountService(storage, logger)

        // Registeriere Event-Listener (funktioniert mit/ohne TrainCarts)
        val listener = RidecountSignListener(service, config.getSignDuplicateCooldownMs())
        server.pluginManager.registerEvents(listener, this)

        // Registriere Commands
        val cmd = RidecountCommand(storage)
        getCommand("ridecount")?.setExecutor(cmd)

        logger.info("Ridecount-System v${description.version} aktiviert.")

        // Prüfe TrainCarts Availability
        val trainCartsPlugin = server.pluginManager.getPlugin("Train_Carts")
        if (trainCartsPlugin != null && trainCartsPlugin.isEnabled) {
            logger.info("TrainCarts erkannt - optimale Sign-Integration aktiv.")
        } else {
            logger.info("TrainCarts nicht geladen - Fallback auf Event-System.")
        }

        if (config.isDebugLoggingEnabled()) {
            logger.info("Debug-Logging ist AKTIVIERT.")
        }
    }

    override fun onDisable() {
        if (::storage.isInitialized) {
            storage.save()
        }
        logger.info("Ridecount-System deaktiviert.")
    }
}
