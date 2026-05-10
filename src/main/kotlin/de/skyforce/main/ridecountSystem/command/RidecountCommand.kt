package de.skyforce.main.ridecountSystem.command

import de.skyforce.main.ridecountSystem.storage.RidecountStorage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class RidecountCommand(
    private val storage: RidecountStorage
) : CommandExecutor, TabCompleter {

    private companion object {
        const val PREFIX = "§8[§6Ridecount§8] §7"
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("ridecount.admin")) {
            sender.sendMessage("${PREFIX}§cDafür hast du keine Berechtigung.")
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
            sender.sendMessage("${PREFIX}§cNutzung: §e/ridecount show <spieler>")
            return true
        }

        val playerName = args[1]
        val player = Bukkit.getPlayer(playerName)

        if (player == null) {
            sender.sendMessage("${PREFIX}§cSpieler '$playerName' ist nicht online.")
            return true
        }

        val stats = storage.getPlayerStats(player.uniqueId)
        if (stats.isEmpty()) {
            sender.sendMessage("${PREFIX}§e${player.name} hat noch keine Einträge.")
            return true
        }

        sender.sendMessage("§8§m----------------------------------------")
        sender.sendMessage("§6Ridecounts von §e${player.name}")
        stats.entries
            .sortedByDescending { it.value }
            .forEach { (attraction, count) ->
                sender.sendMessage("§8- §e${formatAttraction(attraction)}§7: §a$count")
            }
        sender.sendMessage("§8§m----------------------------------------")
        return true
    }

    private fun clearPlayerStats(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.size < 2) {
            sender.sendMessage("${PREFIX}§cNutzung: §e/ridecount clear <spieler> [attraktion]")
            return true
        }

        val playerName = args[1]
        val player = Bukkit.getPlayer(playerName)

        if (player == null) {
            sender.sendMessage("${PREFIX}§cSpieler '$playerName' ist nicht online.")
            return true
        }

        val attraction = if (args.size > 2) args[2] else null

        if (attraction != null) {
            storage.clearPlayerAttraction(player.uniqueId, attraction)
            sender.sendMessage("${PREFIX}§aEintrag für §e${formatAttraction(attraction)} §abei §e${player.name} §agelöscht.")
        } else {
            storage.clearPlayer(player.uniqueId)
            sender.sendMessage("${PREFIX}§aAlle Ridecounts von §e${player.name} §awurden gelöscht.")
        }
        storage.save()
        return true
    }

    private fun showUsage(sender: CommandSender): Boolean {
        sender.sendMessage("§8§m----------------------------------------")
        sender.sendMessage("§6Ridecount - Befehle")
        sender.sendMessage("§e/ridecount show <spieler> §7- Zeigt die Statistik")
        sender.sendMessage("§e/ridecount clear <spieler> [attraktion] §7- Löscht Einträge")
        sender.sendMessage("§8§m----------------------------------------")
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
            2 -> complete(args[1], Bukkit.getOnlinePlayers().map { it.name }.sorted())
            3 -> {
                if (!args[0].equals("clear", ignoreCase = true)) {
                    return emptyList()
                }
                val player = Bukkit.getPlayer(args[1]) ?: return emptyList()
                val attractions = storage.getPlayerStats(player.uniqueId).keys.sorted()
                complete(args[2], attractions)
            }

            else -> emptyList()
        }
    }

    private fun complete(partial: String, values: List<String>): List<String> {
        return values.filter { it.startsWith(partial, ignoreCase = true) }
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
}

