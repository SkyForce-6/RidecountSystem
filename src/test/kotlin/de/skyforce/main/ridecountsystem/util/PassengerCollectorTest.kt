package de.skyforce.main.ridecountsystem.util

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class PassengerCollectorTest {

    @Test
    fun `collects direct and nested player passengers without duplicates`() {
        val first = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val second = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val nestedPlayer = player(first)
        val vehiclePassenger = entity(listOf(nestedPlayer, player(second)))
        val root = entity(listOf(player(first), vehiclePassenger))

        val players = PassengerCollector.collectPlayerIds(root)

        assertEquals(setOf(first, second), players)
    }

    @Test
    fun `collects players from multiple root entities`() {
        val first = UUID.fromString("00000000-0000-0000-0000-000000000003")
        val second = UUID.fromString("00000000-0000-0000-0000-000000000004")

        val players = PassengerCollector.collectPlayerIds(listOf(entity(listOf(player(first))), player(second)))

        assertEquals(setOf(first, second), players)
    }

    private fun player(id: UUID, passengers: List<Entity> = emptyList()): Player {
        return Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getUniqueId" -> id
                "getPassengers" -> passengers
                else -> defaultValue(method.returnType)
            }
        } as Player
    }

    private fun entity(passengers: List<Entity>): Entity {
        return Proxy.newProxyInstance(
            Entity::class.java.classLoader,
            arrayOf(Entity::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getPassengers" -> passengers
                else -> defaultValue(method.returnType)
            }
        } as Entity
    }

    private fun defaultValue(type: Class<*>): Any? {
        return when (type) {
            java.lang.Boolean.TYPE -> false
            java.lang.Byte.TYPE -> 0.toByte()
            java.lang.Short.TYPE -> 0.toShort()
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0f
            java.lang.Double.TYPE -> 0.0
            java.lang.Character.TYPE -> '\u0000'
            java.lang.Void.TYPE -> Unit
            else -> null
        }
    }
}
