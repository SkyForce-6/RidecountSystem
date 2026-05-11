package de.skyforce.main.ridecountSystem.sign

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.util.Vector

object RidecountSignDetector {

    private val plainText = PlainTextComponentSerializer.plainText()

    fun findAttraction(railBlock: Block): String? {
        if (!Tag.RAILS.isTagged(railBlock.type)) {
            return null
        }

        for (direction in CARDINAL_DIRECTIONS) {
            val candidate = railBlock.getRelative(direction.blockX, direction.blockY, direction.blockZ)
            val signState = candidate.state as? Sign ?: continue

            val line0 = readLine(signState, 0)
            val line1 = readLine(signState, 1)
            val line2 = readLine(signState, 2)

            val trainHeaderMatches = line0.equals("[train]", ignoreCase = true) ||
                line0.equals("train", ignoreCase = true)

            if (trainHeaderMatches && line1.equals("ridecount", ignoreCase = true) && line2.isNotEmpty()) {
                return line2
            }
        }

        return null
    }

    @Suppress("DEPRECATION")
    private fun readLine(sign: Sign, index: Int): String {
        return plainText.serialize(sign.lines()[index]).trim()
    }

    private val CARDINAL_DIRECTIONS = listOf(
        Vector(1, 0, 0),
        Vector(-1, 0, 0),
        Vector(0, 0, 1),
        Vector(0, 0, -1)
    ).flatMap { direction ->
        listOf(-1, 0, 1).map { yOffset ->
            Vector(direction.blockX, yOffset, direction.blockZ)
        }
    }
}

