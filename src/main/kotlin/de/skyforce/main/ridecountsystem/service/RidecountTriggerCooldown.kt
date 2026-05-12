package de.skyforce.main.ridecountsystem.service

import org.bukkit.block.Block
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RidecountTriggerCooldown(
    private val cooldownMs: Long
) {

    private val lastTriggerByKey = ConcurrentHashMap<String, Long>()

    fun isAllowed(railBlock: Block, attraction: String, playerIds: Collection<UUID>, fallbackKey: String): Boolean {
        val signKey = "${railBlock.world.uid}:${railBlock.x}:${railBlock.y}:${railBlock.z}:${attraction.lowercase()}"
        val subjectKey = if (playerIds.isNotEmpty()) {
            playerIds.sorted().joinToString(",")
        } else {
            fallbackKey
        }
        val cooldownKey = "$subjectKey:$signKey"

        val now = System.currentTimeMillis()
        val last = lastTriggerByKey[cooldownKey] ?: 0L
        if (now - last < cooldownMs) {
            return false
        }

        lastTriggerByKey[cooldownKey] = now
        return true
    }
}
