package de.skyforce.main.ridecountsystem.command

import de.skyforce.main.ridecountsystem.model.AttractionKey
import de.skyforce.main.ridecountsystem.storage.RidecountStorage
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RidecountCommandTest {

    private companion object {
        fun defaultValue(type: Class<*>): Any? {
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

    @Test
    fun `denies command without admin permission`() {
        val sender = RecordingSender(hasAdminPermission = false)
        val command = RidecountCommand(InMemoryRidecountStorage(), FakePlayerDirectory())

        command.onCommand(sender.proxy, commandStub(), "ridecount", arrayOf("show", "Alex"))

        assertTrue(sender.messages.any { it.contains("keine Berechtigung") })
    }

    @Test
    fun `clears multi word attraction argument`() {
        val playerId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val storage = InMemoryRidecountStorage()
        storage.increment(playerId, "Blue Fire Megacoaster")
        val sender = RecordingSender(hasAdminPermission = true)
        val directory = FakePlayerDirectory(PlayerTarget(playerId, "Alex"))
        val command = RidecountCommand(storage, directory)

        command.onCommand(
            sender.proxy,
            commandStub(),
            "ridecount",
            arrayOf("clear", "Alex", "Blue", "Fire", "Megacoaster")
        )

        assertEquals(emptyMap(), storage.getPlayerStats(playerId))
        assertEquals(1, storage.saveCount)
        assertTrue(sender.messages.any { it.contains("Blue Fire Megacoaster") })
    }

    @Test
    fun `suggests stored attractions for clear subcommand`() {
        val playerId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val storage = InMemoryRidecountStorage()
        storage.increment(playerId, "Blue Fire")
        storage.increment(playerId, "Silver Star")
        val sender = RecordingSender(hasAdminPermission = true)
        val directory = FakePlayerDirectory(PlayerTarget(playerId, "Alex"))
        val command = RidecountCommand(storage, directory)

        val suggestions = command.onTabComplete(sender.proxy, commandStub(), "ridecount", arrayOf("clear", "Alex", "s"))

        assertEquals(listOf("silver_star"), suggestions)
    }

    private fun commandStub(): Command {
        return object : Command("ridecount") {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
                return true
            }
        }
    }

    private class RecordingSender(private val hasAdminPermission: Boolean) {
        val messages = mutableListOf<String>()

        val proxy: CommandSender = Proxy.newProxyInstance(
            CommandSender::class.java.classLoader,
            arrayOf(CommandSender::class.java)
        ) { _, method, args ->
            when (method.name) {
                "hasPermission" -> hasAdminPermission
                "sendMessage" -> {
                    args?.forEach { arg ->
                        when (arg) {
                            is String -> messages += arg
                            is Array<*> -> arg.filterIsInstance<String>().forEach(messages::add)
                        }
                    }
                    Unit
                }
                "isOp" -> false
                "getName" -> "TestSender"
                "spigot" -> null
                else -> defaultValue(method.returnType)
            }
        } as CommandSender
    }

    private class FakePlayerDirectory(private val player: PlayerTarget? = null) : PlayerDirectory {
        override fun resolvePlayer(playerToken: String, storage: RidecountStorage): PlayerTarget? {
            return player?.takeIf { it.displayName.equals(playerToken, ignoreCase = true) }
        }

        override fun knownPlayerSuggestions(storage: RidecountStorage): List<String> {
            return listOfNotNull(player?.displayName)
        }
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
            val key = AttractionKey.fromDisplayName(attraction)
            val stats = values[playerId] ?: return false
            val changed = stats.remove(key) != null
            if (stats.isEmpty()) {
                values.remove(playerId)
            }
            return changed
        }
    }
}
