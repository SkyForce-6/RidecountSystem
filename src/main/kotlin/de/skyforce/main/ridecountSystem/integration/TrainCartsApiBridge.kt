package de.skyforce.main.ridecountSystem.integration

import de.skyforce.main.ridecountSystem.service.RidecountService
import de.skyforce.main.ridecountSystem.sign.RidecountSignDetector
import org.bukkit.entity.Entity
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TrainCartsApiBridge(
    private val plugin: JavaPlugin,
    private val ridecountService: RidecountService,
    private val cooldownMs: Long
) : Listener {

    private val triggerCooldown = ConcurrentHashMap<String, Long>()
    private var registeredToTcEvent = false

    fun init() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        tryRegisterTrainCartsEvent()
    }

    @org.bukkit.event.EventHandler(ignoreCancelled = true)
    fun onPluginEnable(event: PluginEnableEvent) {
        if (!registeredToTcEvent && event.plugin.name.equals("Train_Carts", ignoreCase = true)) {
            tryRegisterTrainCartsEvent()
        }
    }

    private fun tryRegisterTrainCartsEvent() {
        val trainCartsPlugin = plugin.server.pluginManager.getPlugin("Train_Carts")
        if (trainCartsPlugin == null || !trainCartsPlugin.isEnabled) {
            return
        }

        try {
            val eventClass = Class.forName("com.bergerkiller.bukkit.tc.events.MemberBlockChangeEvent")
            if (!Event::class.java.isAssignableFrom(eventClass)) {
                return
            }

            @Suppress("UNCHECKED_CAST")
            val bukkitEventClass = eventClass as Class<out Event>

            plugin.server.pluginManager.registerEvent(
                bukkitEventClass,
                object : Listener {},
                EventPriority.MONITOR,
                { _, event -> handleMemberBlockChange(event) },
                plugin,
                true
            )

            registeredToTcEvent = true
            plugin.logger.info("TrainCarts API Bridge aktiv (MemberBlockChangeEvent).")
        } catch (_: ClassNotFoundException) {
            plugin.logger.info("TrainCarts API Bridge: Event-Klasse nicht gefunden, nutze Bukkit-Fallback.")
        } catch (ex: Exception) {
            plugin.logger.warning("TrainCarts API Bridge konnte nicht registriert werden: ${ex.message}")
        }
    }

    private fun handleMemberBlockChange(event: Event) {
        val member = invokeNoArg(event, "getMember") ?: return

        val minecarts = extractGroupMinecarts(member)
        if (minecarts.isEmpty()) {
            return
        }

        val leadMinecart = minecarts.first()
        val railBlock = leadMinecart.location.block
        val attraction = RidecountSignDetector.findAttraction(railBlock) ?: return

        val trainKey = buildTrainKey(member, minecarts)
        val signKey = "${railBlock.world.uid}:${railBlock.x}:${railBlock.y}:${railBlock.z}:$attraction"
        val cooldownKey = "$trainKey:$signKey"

        val now = System.currentTimeMillis()
        val last = triggerCooldown[cooldownKey] ?: 0L
        if (now - last < cooldownMs) {
            return
        }

        val players = collectPlayersFromMinecarts(minecarts)
        val affected = ridecountService.incrementForPlayers(players, attraction)
        triggerCooldown[cooldownKey] = now

        if (affected > 0) {
            ridecountService.logger.fine("[TrainCarts API] +$affected fuer '$attraction' (train=$trainKey)")
        }
    }

    private fun extractGroupMinecarts(member: Any): List<Minecart> {
        val result = mutableListOf<Minecart>()

        val group = invokeNoArg(member, "getGroup")
        if (group is Iterable<*>) {
            group.forEach { groupMember ->
                extractMinecart(groupMember)?.let(result::add)
            }
            if (result.isNotEmpty()) {
                return result
            }
        }

        extractMinecart(member)?.let(result::add)
        return result
    }

    private fun buildTrainKey(member: Any, minecarts: List<Minecart>): String {
        val group = invokeNoArg(member, "getGroup")
        val groupName = invokeNoArg(group, "getProperties")?.let { properties ->
            invokeNoArg(properties, "getTrainName") as? String
        }

        return groupName ?: minecarts.joinToString(",") { it.uniqueId.toString() }
    }

    private fun collectPlayersFromMinecarts(minecarts: List<Minecart>): Set<UUID> {
        val players = mutableSetOf<UUID>()
        minecarts.forEach { minecart ->
            minecart.passengers.forEach { passenger ->
                collectPlayersRecursive(passenger, players)
            }
        }
        return players
    }

    private fun collectPlayersRecursive(entity: Entity, players: MutableSet<UUID>) {
        if (entity is Player) {
            players += entity.uniqueId
            return
        }

        entity.passengers.forEach { passenger -> collectPlayersRecursive(passenger, players) }
    }

    private fun extractMinecart(source: Any?): Minecart? {
        if (source == null) {
            return null
        }

        if (source is Minecart) {
            return source
        }

        if (source is Entity) {
            return null
        }

        val first = invokeNoArg(source, "getEntity")
        if (first is Minecart) {
            return first
        }
        if (first is Entity) {
            return null
        }

        val second = invokeNoArg(first, "getEntity")
        if (second is Minecart) {
            return second
        }
        if (second is Entity) {
            return null
        }

        return null
    }

    private fun invokeNoArg(instance: Any?, methodName: String): Any? {
        if (instance == null) {
            return null
        }

        return try {
            val method = instance.javaClass.methods.firstOrNull {
                it.name == methodName && it.parameterCount == 0
            } ?: return null
            method.invoke(instance)
        } catch (_: Exception) {
            null
        }
    }
}


