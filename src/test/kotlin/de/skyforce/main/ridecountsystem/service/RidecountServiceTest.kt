package de.skyforce.main.ridecountsystem.service

import de.skyforce.main.ridecountsystem.model.AttractionKey
import de.skyforce.main.ridecountsystem.storage.RidecountStorage
import java.util.UUID
import java.util.logging.Logger
import kotlin.test.Test
import kotlin.test.assertEquals

class RidecountServiceTest {

    private val storage = InMemoryRidecountStorage()
    private val service = RidecountService(storage, Logger.getLogger("RidecountServiceTest"))

    @Test
    fun `increments every player once without immediate save`() {
        val firstPlayer = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val secondPlayer = UUID.fromString("00000000-0000-0000-0000-000000000002")

        val affected = service.incrementForPlayers(listOf(firstPlayer, secondPlayer), "Blue Fire")

        assertEquals(2, affected)
        assertEquals(1, storage.getPlayerStats(firstPlayer)["blue_fire"])
        assertEquals(1, storage.getPlayerStats(secondPlayer)["blue_fire"])
        assertEquals(0, storage.saveCount)
    }

    @Test
    fun `does not save when there are no players`() {
        val affected = service.incrementForPlayers(emptyList(), "Blue Fire")

        assertEquals(0, affected)
        assertEquals(0, storage.saveCount)
    }

    @Test
    fun `does not save when attraction is blank`() {
        val player = UUID.fromString("00000000-0000-0000-0000-000000000001")

        val affected = service.incrementForPlayers(listOf(player), "   ")

        assertEquals(0, affected)
        assertEquals(0, storage.saveCount)
        assertEquals(emptyMap(), storage.getPlayerStats(player))
    }

    private class InMemoryRidecountStorage : RidecountStorage {
        private val values = mutableMapOf<UUID, MutableMap<String, Int>>()
        var saveCount = 0
            private set

        override fun increment(playerId: UUID, attraction: String): Int {
            val key = AttractionKey.fromDisplayName(attraction)
            val stats = values.getOrPut(playerId) { mutableMapOf() }
            val next = stats.getOrDefault(key, 0) + 1
            stats[key] = next
            return next
        }

        override fun save(): Boolean {
            saveCount++
            return true
        }

        override fun getPlayerStats(playerId: UUID): Map<String, Int> {
            return values[playerId]?.toMap() ?: emptyMap()
        }

        override fun getKnownPlayerIds(): Set<UUID> {
            return values.keys
        }

        override fun clearPlayer(playerId: UUID): Boolean {
            return values.remove(playerId) != null
        }

        override fun clearPlayerAttraction(playerId: UUID, attraction: String): Boolean {
            return values[playerId]?.remove(AttractionKey.fromDisplayName(attraction)) != null
        }
    }
}
