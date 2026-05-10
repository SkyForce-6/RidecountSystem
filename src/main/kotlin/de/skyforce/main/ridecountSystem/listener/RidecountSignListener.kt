package de.skyforce.main.ridecountSystem.listener

import de.skyforce.main.ridecountSystem.service.RidecountService
import de.skyforce.main.ridecountSystem.sign.RidecountSignDetector
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RidecountSignListener(
    private val plugin: JavaPlugin,
    private val ridecountService: RidecountService,
    private val cooldownMs: Long = 2000L,
    private val handleVehicleMove: Boolean = true
) : Listener {

    companion object {
        const val PERMISSION_CREATE = "ridecount.sign.create"
        private const val PREFIX = "§8[§6Ridecount§8] §7"
    }

    private val signTriggerCooldown = ConcurrentHashMap<String, Long>()

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

        val attraction = RidecountSignDetector.findAttraction(to) ?: return
        if (attraction.isBlank()) return

        val vehicleId = vehicle.uniqueId
        val worldId = to.world.uid
        val signKey = "$worldId:${to.x}:${to.y}:${to.z}:$attraction"
        val cooldownKey = "$vehicleId:$signKey"

        val lastTrigger = signTriggerCooldown[cooldownKey] ?: 0L
        val now = System.currentTimeMillis()
        if (now - lastTrigger < cooldownMs) return

        val playersInVehicle = collectPlayers(vehicle)
        val affected = ridecountService.incrementForPlayers(playersInVehicle, attraction)
        signTriggerCooldown[cooldownKey] = now

        if (affected > 0) {
            ridecountService.logger.fine(
                "[$signKey] +$affected Spieler für '$attraction' → ${playersInVehicle.size} Gesamt"
            )
        }
    }

    private fun collectPlayers(entity: Entity): Set<UUID> {
        val players = mutableSetOf<UUID>()
        if (entity is Player) players += entity.uniqueId
        entity.passengers.forEach { players += collectPlayers(it) }
        return players
    }
}
