package de.skyforce.main.ridecountsystem.command

import de.skyforce.main.ridecountsystem.storage.RidecountStorage
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.UUID

class RidecountCommand(
    private val storage: RidecountStorage
) : CommandExecutor, TabCompleter {

    private companion object {
        const val PREFIX = "\u00a78[\u00a76Ridecount\u00a78] \u00a77"
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("ridecount.admin")) {
            sender.sendMessage("${PREFIX}\u00a7cDafuer hast du keine Berechtigung.")
            return true
        }

        if (args.isEmpty()) {
            return showUsage(sender)
        }

        return when (args[0].lowercase()) {
            "show" -> showPlayerStats(sender, args)
            "clear" -> clearPlayerStats(sender, args)
            else -> showUsage(sender)
        }
    }

    private fun showPlayerStats(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.size < 2) {
            sender.sendMessage("${PREFIX}\u00a7cNutzung: \u00a7e/ridecount show <spieler|uuid>")
            return true
        }

        val playerToken = args[1]
        val player = resolvePlayer(playerToken)
        if (player == null) {
            sender.sendMessage("${PREFIX}\u00a7cSpieler '$playerToken' wurde nicht gefunden.")
            return true
        }

        val stats = storage.getPlayerStats(player.id)
        if (stats.isEmpty()) {
            sender.sendMessage("${PREFIX}\u00a7e${player.displayName} hat noch keine Eintraege.")
            return true
        }

        sender.sendMessage("\u00a78\u00a7m----------------------------------------")
        sender.sendMessage("\u00a76Ridecounts von \u00a7e${player.displayName}")
        stats.entries
            .sortedByDescending { it.value }
            .forEach { (attraction, count) ->
                sender.sendMessage("\u00a78- \u00a7e${formatAttraction(attraction)}\u00a77: \u00a7a$count")
            }
        sender.sendMessage("\u00a78\u00a7m----------------------------------------")
        return true
    }

    private fun clearPlayerStats(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.size < 2) {
            sender.sendMessage("${PREFIX}\u00a7cNutzung: \u00a7e/ridecount clear <spieler|uuid> [attraktion]")
            return true
        }

        val playerToken = args[1]
        val player = resolvePlayer(playerToken)
        if (player == null) {
            sender.sendMessage("${PREFIX}\u00a7cSpieler '$playerToken' wurde nicht gefunden.")
            return true
        }

        val attraction = if (args.size > 2) args[2] else null
        val changed = if (attraction != null) {
            storage.clearPlayerAttraction(player.id, attraction)
        } else {
            storage.clearPlayer(player.id)
        }

        if (!changed) {
            sender.sendMessage("${PREFIX}\u00a7eKeine passenden Ridecount-Eintraege fuer \u00a7e${player.displayName}\u00a77 gefunden.")
            return true
        }

        storage.save()
        if (attraction != null) {
            sender.sendMessage("${PREFIX}\u00a7aEintrag fuer \u00a7e${formatAttraction(attraction)} \u00a7abei \u00a7e${player.displayName} \u00a7ageloescht.")
        } else {
            sender.sendMessage("${PREFIX}\u00a7aAlle Ridecounts von \u00a7e${player.displayName} \u00a7awurden geloescht.")
        }
        return true
    }

    private fun showUsage(sender: CommandSender): Boolean {
        sender.sendMessage("\u00a78\u00a7m----------------------------------------")
        sender.sendMessage("\u00a76Ridecount - Befehle")
        sender.sendMessage("\u00a7e/ridecount show <spieler|uuid> \u00a77- Zeigt die Statistik")
        sender.sendMessage("\u00a7e/ridecount clear <spieler|uuid> [attraktion] \u00a77- Loescht Eintraege")
        sender.sendMessage("\u00a78\u00a7m----------------------------------------")
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (!sender.hasPermission("ridecount.admin")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> complete(args[0], listOf("show", "clear"))
            2 -> complete(args[1], knownPlayerSuggestions())
            3 -> {
                if (!args[0].equals("clear", ignoreCase = true)) {
                    return emptyList()
                }
                val player = resolvePlayer(args[1]) ?: return emptyList()
                complete(args[2], storage.getPlayerStats(player.id).keys.sorted())
            }

            else -> emptyList()
        }
    }

    private fun complete(partial: String, values: List<String>): List<String> {
        return values.filter { it.startsWith(partial, ignoreCase = true) }
    }

    private fun resolvePlayer(playerToken: String): PlayerTarget? {
        Bukkit.getPlayerExact(playerToken)?.let { player ->
            return PlayerTarget(player.uniqueId, player.name)
        }

        parseUuid(playerToken)?.let { id ->
            val offlinePlayer = Bukkit.getOfflinePlayer(id)
            if (offlinePlayer.isKnown() || storage.getPlayerStats(id).isNotEmpty()) {
                return PlayerTarget(id, offlinePlayer.name ?: id.toString())
            }
        }

        val offlinePlayer = Bukkit.getOfflinePlayer(playerToken)
        if (offlinePlayer.isKnown() || storage.getPlayerStats(offlinePlayer.uniqueId).isNotEmpty()) {
            return PlayerTarget(offlinePlayer.uniqueId, offlinePlayer.name ?: playerToken)
        }

        return storage.getKnownPlayerIds()
            .asSequence()
            .map { id -> id to Bukkit.getOfflinePlayer(id) }
            .firstOrNull { (_, storedPlayer) ->
                storedPlayer.name?.equals(playerToken, ignoreCase = true) == true
            }
            ?.let { (id, storedPlayer) -> PlayerTarget(id, storedPlayer.name ?: id.toString()) }
    }

    private fun OfflinePlayer.isKnown(): Boolean {
        return isOnline || hasPlayedBefore()
    }

    private fun knownPlayerSuggestions(): List<String> {
        val onlineNames = Bukkit.getOnlinePlayers().map { it.name }
        val storedNames = storage.getKnownPlayerIds().map { id ->
            Bukkit.getOfflinePlayer(id).name ?: id.toString()
        }
        return (onlineNames + storedNames).distinct().sorted()
    }

    private fun parseUuid(raw: String): UUID? {
        return try {
            UUID.fromString(raw)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun formatAttraction(raw: String): String {
        return raw
            .replace('_', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

    private data class PlayerTarget(
        val id: UUID,
        val displayName: String
    )
}
