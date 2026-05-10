package de.skyforce.main.ridecountSystem.listener

import de.skyforce.main.ridecountSystem.service.RidecountService
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Entity
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RidecountSignListener(
    private val ridecountService: RidecountService,
    private val cooldownMs: Long = 2000L
) : Listener {

    private val signTriggerCooldown = ConcurrentHashMap<String, Long>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onVehicleMove(event: VehicleMoveEvent) {
        val vehicle = event.vehicle
        if (vehicle !is Minecart) {
            return
        }

        val from = event.from.block
        val to = event.to.block
        if (from.x == to.x && from.y == to.y && from.z == to.z) {
            return
        }

        val attraction = findRidecountAttraction(to)
        if (attraction.isBlank()) {
            return
        }

        val vehicleId = vehicle.uniqueId
        val worldId = to.world.uid
        val signKey = "$worldId:${to.x}:${to.y}:${to.z}:$attraction"
        val cooldownKey = "$vehicleId:$signKey"

        val lastTrigger = signTriggerCooldown[cooldownKey] ?: 0L
        val now = System.currentTimeMillis()
        if (now - lastTrigger < cooldownMs) {
            return
        }

        val playersInVehicle = collectPlayers(vehicle)
        val affected = ridecountService.incrementForPlayers(playersInVehicle, attraction)
        signTriggerCooldown[cooldownKey] = now

        if (affected > 0) {
            ridecountService.logger.fine(
                "[$signKey] +$affected Spieler für '$attraction' → ${playersInVehicle.size} Gesamt"
            )
        }
    }

    private fun findRidecountAttraction(railBlock: Block): String {
        if (!Tag.RAILS.isTagged(railBlock.type)) {
            return ""
        }

        for (direction in CARDINAL_DIRECTIONS) {
            val candidate = railBlock.getRelative(direction.blockX, direction.blockY, direction.blockZ)
            val signState = candidate.state as? Sign ?: continue

            val line0 = signState.getLine(0).trim()
            val line1 = signState.getLine(1).trim()
            val line2 = signState.getLine(2).trim()

            val trainHeaderMatches = line0.equals("[train]", ignoreCase = true) ||
                line0.equals("train", ignoreCase = true)

            if (trainHeaderMatches && line1.equals("ridecount", ignoreCase = true) && line2.isNotEmpty()) {
                return line2
            }
        }

        return ""
    }

    private fun collectPlayers(entity: Entity): Set<UUID> {
        val players = mutableSetOf<UUID>()

        if (entity is Player) {
            players += entity.uniqueId
        }

        entity.passengers.forEach { passenger ->
            players += collectPlayers(passenger)
        }

        return players
    }

    private companion object {
        val CARDINAL_DIRECTIONS = listOf(
            Vector(1, 0, 0),
            Vector(-1, 0, 0),
            Vector(0, 0, 1),
            Vector(0, 0, -1),
            Vector(1, -1, 0),
            Vector(-1, -1, 0),
            Vector(0, -1, 1),
            Vector(0, -1, -1)
        )
    }
}

