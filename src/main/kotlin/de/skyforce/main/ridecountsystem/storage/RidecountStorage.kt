package de.skyforce.main.ridecountsystem.storage

import java.util.UUID

interface RidecountStorage {
    fun increment(playerId: UUID, attraction: String): Int
    fun save(): Boolean
    fun getPlayerStats(playerId: UUID): Map<String, Int>
    fun getKnownPlayerIds(): Set<UUID>
    fun clearPlayer(playerId: UUID): Boolean
    fun clearPlayerAttraction(playerId: UUID, attraction: String): Boolean
}

