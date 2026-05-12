package de.skyforce.main.ridecountsystem.listener

import de.skyforce.main.ridecountsystem.RidecountSystem
import de.skyforce.main.ridecountsystem.service.RidecountService
import de.skyforce.main.ridecountsystem.service.RidecountTriggerCooldown
import de.skyforce.main.ridecountsystem.storage.RidecountStorage
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.block.SignChangeEvent
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import java.util.UUID
import java.util.logging.Logger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RidecountSignListenerMockBukkitTest {

    private lateinit var server: ServerMock
    private lateinit var plugin: RidecountSystem
    private lateinit var listener: RidecountSignListener

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(RidecountSystem::class.java)
        listener = RidecountSignListener(
            plugin = plugin,
            ridecountService = RidecountService(NoopStorage, Logger.getLogger("RidecountSignListenerMockBukkitTest")),
            triggerCooldown = RidecountTriggerCooldown(2_000L)
        )
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `cancels ridecount sign creation without permission`() {
        val player = server.addPlayer("Alex")
        player.setOp(false)
        val event = signChangeEvent("[train]", "ridecount", "Blue Fire", player)

        listener.onSignChange(event)

        assertTrue(event.isCancelled)
    }

    @Test
    fun `cancels ridecount sign creation without attraction name`() {
        val player = server.addPlayer("Alex")
        player.setOp(true)
        val event = signChangeEvent("[train]", "ridecount", "   ", player)

        listener.onSignChange(event)

        assertTrue(event.isCancelled)
    }

    @Test
    fun `accepts valid ridecount sign from permitted player`() {
        val player = server.addPlayer("Alex")
        player.setOp(true)
        val event = signChangeEvent("[train]", "ridecount", "Blue Fire", player)

        listener.onSignChange(event)

        assertFalse(event.isCancelled)
    }

    @Suppress("DEPRECATION")
    private fun signChangeEvent(
        line0: String,
        line1: String,
        line2: String,
        player: org.bukkit.entity.Player
    ): SignChangeEvent {
        val world = server.addSimpleWorld("world-${UUID.randomUUID()}")
        val block = world.getBlockAt(0, 64, 0)
        block.type = Material.OAK_SIGN
        return SignChangeEvent(
            block,
            player,
            listOf(Component.text(line0), Component.text(line1), Component.text(line2), Component.empty())
        )
    }

    private object NoopStorage : RidecountStorage {
        override fun increment(playerId: UUID, attraction: String): Int = 0
        override fun save(): Boolean = false
        override fun getPlayerStats(playerId: UUID): Map<String, Int> = emptyMap()
        override fun getKnownPlayerIds(): Set<UUID> = emptySet()
        override fun clearPlayer(playerId: UUID): Boolean = false
        override fun clearPlayerAttraction(playerId: UUID, attraction: String): Boolean = false
    }
}
