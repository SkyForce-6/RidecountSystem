package de.skyforce.main.ridecountSystem.storage

import java.util.UUID

interface RidecountStorage {
    fun increment(playerId: UUID, attraction: String): Int
    fun save()
    fun getPlayerStats(playerId: UUID): Map<String, Int>
    fun clearPlayer(playerId: UUID)
    fun clearPlayerAttraction(playerId: UUID, attraction: String)
}

