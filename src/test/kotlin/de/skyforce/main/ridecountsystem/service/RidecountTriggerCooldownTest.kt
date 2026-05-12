package de.skyforce.main.ridecountsystem.service

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RidecountTriggerCooldownTest {

    @Test
    fun `blocks duplicate trigger for same players and sign within cooldown`() {
        var now = 0L
        val cooldown = RidecountTriggerCooldown(2_000L) { now }
        val player = UUID.fromString("00000000-0000-0000-0000-000000000001")

        assertTrue(cooldown.isAllowed("world:1:2:3:blue_fire", listOf(player), "cart-a"))
        now = 1_999L
        assertFalse(cooldown.isAllowed("world:1:2:3:blue_fire", listOf(player), "cart-b"))
        now = 2_000L
        assertTrue(cooldown.isAllowed("world:1:2:3:blue_fire", listOf(player), "cart-b"))
    }

    @Test
    fun `uses sorted player set as stable subject key`() {
        var now = 10_000L
        val cooldown = RidecountTriggerCooldown(2_000L) { now }
        val first = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val second = UUID.fromString("00000000-0000-0000-0000-000000000002")

        assertTrue(cooldown.isAllowed("world:1:2:3:blue_fire", listOf(first, second), "train-a"))
        now = 10_100L
        assertFalse(cooldown.isAllowed("world:1:2:3:blue_fire", listOf(second, first), "train-b"))
    }

    @Test
    fun `removes expired entries during cleanup`() {
        var now = 0L
        val cooldown = RidecountTriggerCooldown(1_000L) { now }

        assertTrue(cooldown.isAllowed("world:1:2:3:blue_fire", emptyList(), "train-a"))
        assertEquals(1, cooldown.trackedEntryCount())

        now = 3_001L
        assertTrue(cooldown.isAllowed("world:4:5:6:silver_star", emptyList(), "train-b"))

        assertEquals(1, cooldown.trackedEntryCount())
    }
}
