package de.skyforce.main.ridecountsystem.integration

import de.skyforce.main.ridecountsystem.service.RidecountService
import de.skyforce.main.ridecountsystem.service.RidecountTriggerCooldown
import de.skyforce.main.ridecountsystem.sign.RidecountSignDetector
import de.skyforce.main.ridecountsystem.util.PassengerCollector
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Minecart
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class TrainCartsApiBridge(
    private val plugin: JavaPlugin,
    private val ridecountService: RidecountService,
    private val triggerCooldown: RidecountTriggerCooldown,
    private val onNativeBridgeEnabled: () -> Unit = {}
) : Listener {

    private var registeredToTcEvent = false

    fun init(): Boolean {
        plugin.server.pluginManager.registerEvents(this, plugin)
        return tryRegisterTrainCartsEvent()
    }

    @org.bukkit.event.EventHandler(ignoreCancelled = true)
    fun onPluginEnable(event: PluginEnableEvent) {
        if (!registeredToTcEvent && event.plugin.name.equals("Train_Carts", ignoreCase = true)) {
            if (tryRegisterTrainCartsEvent()) {
                onNativeBridgeEnabled()
            }
        }
    }

    private fun tryRegisterTrainCartsEvent(): Boolean {
        if (registeredToTcEvent) {
            return true
        }

        val trainCartsPlugin = plugin.server.pluginManager.getPlugin("Train_Carts")
        if (trainCartsPlugin == null || !trainCartsPlugin.isEnabled) {
            return false
        }

        try {
            val eventClass = Class.forName("com.bergerkiller.bukkit.tc.events.MemberBlockChangeEvent")
            if (!Event::class.java.isAssignableFrom(eventClass)) {
                return false
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
            return true
        } catch (_: ClassNotFoundException) {
            plugin.logger.info("TrainCarts API Bridge: Event-Klasse nicht gefunden, nutze Bukkit-Fallback.")
        } catch (ex: Exception) {
            plugin.logger.log(Level.WARNING, "TrainCarts API Bridge konnte nicht registriert werden.", ex)
        }

        return false
    }

    private fun handleMemberBlockChange(event: Event) {
        val member = invokeNoArg(event, "getMember") ?: return

        val minecarts = extractGroupMinecarts(member)
        if (minecarts.isEmpty()) {
            return
        }

        val railBlock = resolveRailBlock(event, minecarts.first()) ?: return
        val attraction = RidecountSignDetector.findAttraction(railBlock) ?: return

        val players = PassengerCollector.collectPlayerIds(minecarts.flatMap { it.passengers })
        val trainKey = buildTrainKey(member, minecarts)
        val allowed = triggerCooldown.isAllowed(
            railBlock = railBlock,
            attraction = attraction,
            playerIds = players,
            fallbackKey = trainKey
        )
        if (!allowed) return

        val affected = ridecountService.incrementForPlayers(players, attraction)

        if (affected > 0) {
            ridecountService.logger.fine("[TrainCarts API] +$affected fuer '$attraction' (train=$trainKey)")
        }
    }

    private fun resolveRailBlock(event: Event, fallbackMinecart: Minecart): Block? {
        val eventBlock = invokeNoArg(event, "getToBlock") as? Block
        if (eventBlock != null && Tag.RAILS.isTagged(eventBlock.type)) {
            return eventBlock
        }

        val minecartBlock = fallbackMinecart.location.block
        if (Tag.RAILS.isTagged(minecartBlock.type)) {
            return minecartBlock
        }

        val blockBelow = minecartBlock.getRelative(0, -1, 0)
        return blockBelow.takeIf { Tag.RAILS.isTagged(it.type) }
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


