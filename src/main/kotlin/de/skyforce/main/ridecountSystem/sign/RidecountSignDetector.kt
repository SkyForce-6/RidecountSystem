package de.skyforce.main.ridecountSystem.sign

import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.util.Vector

object RidecountSignDetector {

    fun findAttraction(railBlock: Block): String? {
        if (!Tag.RAILS.isTagged(railBlock.type)) {
            return null
        }

        for (direction in CARDINAL_DIRECTIONS) {
            val candidate = railBlock.getRelative(direction.blockX, direction.blockY, direction.blockZ)
            val signState = candidate.state as? Sign ?: continue

            val line0 = signState.getLine(0).trim()
            val line1 = signState.getLine(1).trim()
            val line2 = signState.getLine(2).trim()

            val trainHeaderMatches = line0.equals("[train]", ignoreCase = true) ||
                line0.equals("train", ignoreCase = true)

            if (trainHeaderMatches && line1.equals("ridecount", ignoreCase = true) && line2.isNotEmpty()) {
                return line2
            }
        }

        return null
    }

    private val CARDINAL_DIRECTIONS = listOf(
        Vector(1, 0, 0),
        Vector(-1, 0, 0),
        Vector(0, 0, 1),
        Vector(0, 0, -1),
        Vector(1, -1, 0),
        Vector(-1, -1, 0),
        Vector(0, -1, 1),
        Vector(0, -1, -1)
    )
}

