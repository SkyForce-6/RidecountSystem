package de.skyforce.main.ridecountsystem

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RidecountSystemMockBukkitTest {

    private lateinit var server: ServerMock
    private lateinit var plugin: RidecountSystem
    private val plainText = PlainTextComponentSerializer.plainText()

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(RidecountSystem::class.java)
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `plugin loads config storage and ridecount command`() {
        assertTrue(plugin.isEnabled)
        assertTrue(plugin.dataFolder.resolve("config.yml").exists())
        assertTrue(plugin.dataFolder.resolve("ridecounts.yml").exists())
        assertNotNull(plugin.getCommand("ridecount"))
    }

    @Test
    fun `registered command handles online admin player`() {
        val player = adminPlayer("Alex")

        val dispatched = server.dispatchCommand(player, "ridecount show Alex")

        assertTrue(dispatched)
        assertTrue(nextPlainMessage(player).contains("noch keine Eintraege"))
    }

    @Test
    fun `registered command rejects player without admin permission`() {
        val player = server.addPlayer("Alex")
        player.setOp(false)

        val dispatched = server.dispatchCommand(player, "ridecount show Alex")

        assertTrue(dispatched)
        val message = nextPlainMessage(player)
        assertTrue(message.contains("Berechtigung") || message.contains("permission", ignoreCase = true), message)
    }

    private fun adminPlayer(name: String): PlayerMock {
        val player = server.addPlayer(name)
        player.setOp(true)
        return player
    }

    private fun nextPlainMessage(player: PlayerMock): String {
        return plainText.serialize(requireNotNull(player.nextComponentMessage()))
    }
}
