package de.skyforce.main.ridecountsystem.storage

import java.nio.file.Files
import java.util.UUID
import java.util.logging.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class YamlRidecountStorageTest {

    private val logger = Logger.getLogger("YamlRidecountStorageTest")

    @Test
    fun `save skips clean storage and persists dirty changes`() {
        val file = Files.createTempDirectory("ridecount-storage").resolve("ridecounts.yml").toFile()
        val playerId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val storage = YamlRidecountStorage(file, logger)

        assertFalse(storage.save())

        assertEquals(1, storage.increment(playerId, "Blue Fire"))
        assertTrue(storage.save())
        assertFalse(storage.save())

        val reloaded = YamlRidecountStorage(file, logger)
        assertEquals(mapOf("blue_fire" to 1), reloaded.getPlayerStats(playerId))
        assertEquals(setOf(playerId), reloaded.getKnownPlayerIds())
    }

    @Test
    fun `clear methods only save when data changed`() {
        val file = Files.createTempDirectory("ridecount-storage").resolve("ridecounts.yml").toFile()
        val playerId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val storage = YamlRidecountStorage(file, logger)

        assertFalse(storage.clearPlayer(playerId))
        assertFalse(storage.clearPlayerAttraction(playerId, "Blue Fire"))
        assertFalse(storage.save())

        storage.increment(playerId, "Blue Fire")
        storage.increment(playerId, "Silver Star")

        assertTrue(storage.clearPlayerAttraction(playerId, "Blue Fire"))
        assertTrue(storage.save())
        assertEquals(mapOf("silver_star" to 1), YamlRidecountStorage(file, logger).getPlayerStats(playerId))

        assertTrue(storage.clearPlayer(playerId))
        assertTrue(storage.save())
        assertEquals(emptyMap(), YamlRidecountStorage(file, logger).getPlayerStats(playerId))
    }

    @Test
    fun `known player ids ignores malformed storage keys`() {
        val file = Files.createTempDirectory("ridecount-storage").resolve("ridecounts.yml").toFile()
        val playerId = UUID.fromString("00000000-0000-0000-0000-000000000003")
        file.writeText(
            """
            players:
              $playerId:
                blue_fire: 2
              not-a-uuid:
                blue_fire: 1
            """.trimIndent()
        )

        val storage = YamlRidecountStorage(file, logger)

        assertEquals(setOf(playerId), storage.getKnownPlayerIds())
    }

    @Test
    fun `rejects empty normalized attraction keys`() {
        val file = Files.createTempDirectory("ridecount-storage").resolve("ridecounts.yml").toFile()
        val playerId = UUID.fromString("00000000-0000-0000-0000-000000000004")
        val storage = YamlRidecountStorage(file, logger)

        assertFailsWith<IllegalArgumentException> {
            storage.increment(playerId, " ### ")
        }
        assertFalse(storage.clearPlayerAttraction(playerId, " ### "))
    }
}
