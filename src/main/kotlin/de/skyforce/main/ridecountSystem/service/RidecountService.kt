package de.skyforce.main.ridecountSystem.service

import de.skyforce.main.ridecountSystem.storage.RidecountStorage
import java.util.UUID
import java.util.logging.Logger

class RidecountService(
    private val storage: RidecountStorage,
    val logger: Logger
) {

    fun incrementForPlayers(playerIds: Collection<UUID>, attraction: String): Int {
        if (playerIds.isEmpty()) {
            logger.fine("Keine Spieler in Zug gefunden.")
            return 0
        }

        val normalizedAttraction = attraction.trim()
        if (normalizedAttraction.isEmpty()) {
            logger.warning("Attraktionsname ist leer.")
            return 0
        }

        var incrementedCount = 0
        playerIds.forEach { playerId ->
            val newCount = storage.increment(playerId, normalizedAttraction)
            incrementedCount++
            logger.finest("$playerId → $normalizedAttraction: $newCount")
        }

        storage.save()
        return incrementedCount
    }
}

