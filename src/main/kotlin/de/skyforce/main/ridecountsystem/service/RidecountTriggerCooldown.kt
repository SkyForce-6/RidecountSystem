package de.skyforce.main.ridecountsystem.service

import org.bukkit.block.Block
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RidecountTriggerCooldown(
    private val cooldownMs: Long,
    private val clock: () -> Long = System::currentTimeMillis
) {

    private val lastTriggerByKey = ConcurrentHashMap<String, Long>()
    private var lastCleanupAt = 0L

    fun isAllowed(railBlock: Block, attraction: String, playerIds: Collection<UUID>, fallbackKey: String): Boolean {
        val signKey = "${railBlock.world.uid}:${railBlock.x}:${railBlock.y}:${railBlock.z}:${attraction.lowercase()}"
        return isAllowed(signKey, playerIds, fallbackKey)
    }

    internal fun isAllowed(signKey: String, playerIds: Collection<UUID>, fallbackKey: String): Boolean {
        val subjectKey = if (playerIds.isNotEmpty()) {
            playerIds.sorted().joinToString(",")
        } else {
            fallbackKey
        }
        val cooldownKey = "$subjectKey:$signKey"

        val now = clock()
        cleanupExpiredEntries(now)
        val last = lastTriggerByKey[cooldownKey]
        if (last != null && now - last < cooldownMs) {
            return false
        }

        lastTriggerByKey[cooldownKey] = now
        return true
    }

    internal fun trackedEntryCount(): Int {
        return lastTriggerByKey.size
    }

    private fun cleanupExpiredEntries(now: Long) {
        val cleanupIntervalMs = cooldownMs.coerceAtLeast(1_000L)
        if (now - lastCleanupAt < cleanupIntervalMs) {
            return
        }

        val retentionMs = cooldownMs.coerceAtLeast(1L) * 2
        lastTriggerByKey.entries.removeIf { (_, lastTriggerAt) -> now - lastTriggerAt > retentionMs }
        lastCleanupAt = now
    }
}
