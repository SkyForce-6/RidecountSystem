package de.skyforce.main.ridecountsystem.listener

import de.skyforce.main.ridecountsystem.service.RidecountService
import de.skyforce.main.ridecountsystem.service.RidecountTriggerCooldown
import de.skyforce.main.ridecountsystem.sign.RidecountSignDetector
import de.skyforce.main.ridecountsystem.util.PassengerCollector
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.Minecart
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.plugin.java.JavaPlugin

class RidecountSignListener(
    private val plugin: JavaPlugin,
    private val ridecountService: RidecountService,
    private val triggerCooldown: RidecountTriggerCooldown,
    handleVehicleMove: Boolean = true
) : Listener {

    companion object {
        const val PERMISSION_CREATE = "ridecount.sign.create"
        private const val PREFIX = "§8[§6Ridecount§8] §7"
    }

    @Volatile
    private var handleVehicleMove: Boolean = handleVehicleMove

    fun setVehicleMoveFallbackEnabled(enabled: Boolean) {
        handleVehicleMove = enabled
    }

    @Suppress("DEPRECATION")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onSignChange(event: SignChangeEvent) {
        val line0 = event.getLine(0)?.trim() ?: return
        val line1 = event.getLine(1)?.trim() ?: return
        val line2 = event.getLine(2)?.trim() ?: return

        val isTrainHeader = line0.equals("[train]", ignoreCase = true) ||
            line0.equals("train", ignoreCase = true)

        if (!isTrainHeader || !line1.equals("ridecount", ignoreCase = true)) {
            return
        }

        val player = event.player

        if (!player.hasPermission(PERMISSION_CREATE)) {
            event.isCancelled = true
            player.sendMessage("${PREFIX}§cDu hast keine Berechtigung, Ridecount-Schilder zu erstellen.")
            val block = event.block
            plugin.server.scheduler.runTask(plugin) { _ -> block.type = Material.AIR }
            return
        }

        if (line2.isBlank()) {
            event.isCancelled = true
            player.sendMessage("${PREFIX}§cBitte gib in Zeile 3 den Namen der Attraktion an.")
            val block = event.block
            plugin.server.scheduler.runTask(plugin) { _ -> block.type = Material.AIR }
            return
        }

        player.sendMessage("${PREFIX}§aRidecount-Schild für §e$line2 §aerfolgreich erstellt.")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onVehicleMove(event: VehicleMoveEvent) {
        if (!handleVehicleMove) return

        val vehicle = event.vehicle
        if (vehicle !is Minecart) return

        val from = event.from.block
        val to = event.to.block
        if (from.x == to.x && from.y == to.y && from.z == to.z) return

        val railBlock = resolveRailBlock(to, vehicle) ?: return
        val attraction = RidecountSignDetector.findAttraction(railBlock) ?: return
        if (attraction.isBlank()) return

        val playersInVehicle = PassengerCollector.collectPlayerIds(vehicle)
        val allowed = triggerCooldown.isAllowed(
            railBlock = railBlock,
            attraction = attraction,
            playerIds = playersInVehicle,
            fallbackKey = vehicle.uniqueId.toString()
        )
        if (!allowed) return

        val affected = ridecountService.incrementForPlayers(playersInVehicle, attraction)

        if (affected > 0) {
            ridecountService.logger.fine(
                "[${railBlock.world.name}:${railBlock.x}:${railBlock.y}:${railBlock.z}] +$affected Spieler fuer '$attraction'"
            )
        }
    }

    private fun resolveRailBlock(toBlock: Block, vehicle: Minecart): Block? {
        if (Tag.RAILS.isTagged(toBlock.type)) {
            return toBlock
        }

        val belowTo = toBlock.getRelative(0, -1, 0)
        if (Tag.RAILS.isTagged(belowTo.type)) {
            return belowTo
        }

        val vehicleBlock = vehicle.location.block
        if (Tag.RAILS.isTagged(vehicleBlock.type)) {
            return vehicleBlock
        }

        val belowVehicle = vehicleBlock.getRelative(0, -1, 0)
        return belowVehicle.takeIf { Tag.RAILS.isTagged(it.type) }
    }
}
