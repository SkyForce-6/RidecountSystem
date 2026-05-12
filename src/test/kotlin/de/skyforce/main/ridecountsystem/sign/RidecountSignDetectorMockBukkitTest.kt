package de.skyforce.main.ridecountsystem.sign

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RidecountSignDetectorMockBukkitTest {

    private lateinit var server: ServerMock

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `finds attraction on adjacent train ridecount sign`() {
        val world = server.addSimpleWorld("world")
        val rail = world.getBlockAt(0, 64, 0)
        rail.type = Material.RAIL
        val signBlock = world.getBlockAt(1, 64, 0)
        signBlock.type = Material.OAK_SIGN
        val sign = signBlock.state as Sign
        val front = sign.getSide(Side.FRONT)
        front.line(0, Component.text("[train]"))
        front.line(1, Component.text("ridecount"))
        front.line(2, Component.text("Blue Fire"))
        sign.update()

        assertEquals("Blue Fire", RidecountSignDetector.findAttraction(rail))
    }

    @Test
    fun `ignores non rail blocks`() {
        val world = server.addSimpleWorld("world")
        val block = world.getBlockAt(0, 64, 0)
        block.type = Material.STONE

        assertNull(RidecountSignDetector.findAttraction(block))
    }
}
