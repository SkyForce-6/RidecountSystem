package de.skyforce.main.ridecountSystem.command

import de.skyforce.main.ridecountSystem.storage.RidecountStorage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RidecountCommand(
    private val storage: RidecountStorage
) : CommandExecutor {

    private companion object {
        const val PREFIX = "§8[§6Ridecount§8] §7"
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("ridecount.admin")) {
            sender.sendMessage("${PREFIX}§cDafuer hast du keine Berechtigung.")
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
            sender.sendMessage("${PREFIX}§e${player.name} hat noch keine Ridecounts.")
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
            sender.sendMessage("${PREFIX}§aEintrag fuer §e${formatAttraction(attraction)} §avon §e${player.name} §ageloescht.")
        } else {
            storage.clearPlayer(player.uniqueId)
            sender.sendMessage("${PREFIX}§aAlle Ridecounts von §e${player.name} §awurden geloescht.")
        }
        storage.save()
        return true
    }

    private fun showUsage(sender: CommandSender): Boolean {
        sender.sendMessage("§8§m----------------------------------------")
        sender.sendMessage("§6Ridecount - Befehle")
        sender.sendMessage("§e/ridecount show <spieler> §7- Zeigt Ridecounts")
        sender.sendMessage("§e/ridecount clear <spieler> [attraktion] §7- Loescht Ridecounts")
        sender.sendMessage("§8§m----------------------------------------")
        return true
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

