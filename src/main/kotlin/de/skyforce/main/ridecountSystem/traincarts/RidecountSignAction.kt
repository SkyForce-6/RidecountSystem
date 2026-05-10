package de.skyforce.main.ridecountSystem.traincarts

import de.skyforce.main.ridecountSystem.service.RidecountService
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import java.util.UUID

/**
 * TrainCarts SignAction für automatische Ridecount-Registrierung
 * Wird über Reflection registriert, wenn TrainCarts geladen ist.
 * Sign-Format:
 * ```
 * [train]
 * ridecount
 * <attraction-name>
 */
class RidecountSignAction(
    private val ridecountService: RidecountService
) {

    /**
     * Wird von TrainCarts aufgerufen, wenn ein Zug ein Schild mit
     * den korrekten Zeilen passiert.
     */
    fun handleMinecartPassage(minecart: Minecart, attraction: String): Boolean {
        if (attraction.isBlank()) {
            return false
        }

        val playersInCart = collectPlayersFromMinecart(minecart)
        val affected = ridecountService.incrementForPlayers(playersInCart, attraction)

        if (affected > 0) {
            ridecountService.logger.fine(
                "[TrainCarts] +$affected Spieler für '$attraction' (Zug ${minecart.uniqueId})"
            )
        }

        return true
    }

    private fun collectPlayersFromMinecart(minecart: Minecart): Set<UUID> {
        val players = mutableSetOf<UUID>()

        val passengers = minecart.passengers
        passengers.forEach { passenger ->
            collectPlayersRecursive(passenger, players)
        }

        return players
    }

    private fun collectPlayersRecursive(entity: org.bukkit.entity.Entity, players: MutableSet<UUID>) {
        if (entity is Player) {
            players += entity.uniqueId
            return
        }

        entity.passengers.forEach { passenger ->
            collectPlayersRecursive(passenger, players)
        }
    }
}



