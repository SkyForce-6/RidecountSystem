package de.skyforce.main.ridecountSystem.util

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.UUID

object PassengerCollector {

    fun collectPlayerIds(entities: Iterable<Entity>): Set<UUID> {
        val players = linkedSetOf<UUID>()
        entities.forEach { collectPlayerIds(it, players) }
        return players
    }

    fun collectPlayerIds(entity: Entity): Set<UUID> {
        val players = linkedSetOf<UUID>()
        collectPlayerIds(entity, players)
        return players
    }

    private fun collectPlayerIds(entity: Entity, players: MutableSet<UUID>) {
        if (entity is Player) {
            players += entity.uniqueId
        }

        entity.passengers.forEach { passenger ->
            collectPlayerIds(passenger, players)
        }
    }
}
