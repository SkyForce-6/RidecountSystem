package de.skyforce.main.ridecountSystem.command

import de.skyforce.main.ridecountSystem.storage.RidecountStorage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

class RidecountCommand(
    private val storage: RidecountStorage
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("ridecount.admin")) {
            sender.sendMessage("§cKeine Berechtigung.")
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
            sender.sendMessage("§cNutzung: /ridecount show <spieler>")
            return true
        }

        val playerName = args[1]
        val player = Bukkit.getPlayer(playerName)

        if (player == null) {
            sender.sendMessage("§cSpieler '$playerName' nicht online.")
            return true
        }

        val stats = storage.getPlayerStats(player.uniqueId)
        if (stats.isEmpty()) {
            sender.sendMessage("§e${player.name} hat noch keine Fahrten geloggt.")
            return true
        }

        sender.sendMessage("§6=== Ridecount für ${player.name} ===")
        stats.forEach { (attraction, count) ->
            sender.sendMessage("§e$attraction§f: §a$count")
        }
        return true
    }

    private fun clearPlayerStats(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.size < 2) {
            sender.sendMessage("§cNutzung: /ridecount clear <spieler> [attraktion]")
            return true
        }

        val playerName = args[1]
        val player = Bukkit.getPlayer(playerName)

        if (player == null) {
            sender.sendMessage("§cSpieler '$playerName' nicht online.")
            return true
        }

        val attraction = if (args.size > 2) args[2] else null

        if (attraction != null) {
            storage.clearPlayerAttraction(player.uniqueId, attraction)
            sender.sendMessage("§a${player.name}'s Counts für '$attraction' gelöscht.")
        } else {
            storage.clearPlayer(player.uniqueId)
            sender.sendMessage("§a${player.name}'s alle Counts gelöscht.")
        }
        storage.save()
        return true
    }

    private fun showUsage(sender: CommandSender): Boolean {
        sender.sendMessage("§6Ridecount-Befehle")
        sender.sendMessage("§e/ridecount show <spieler>§f - Zeige Statistiken")
        sender.sendMessage("§e/ridecount clear <spieler> [attraktion]§f - Lösche Counts")
        return true
    }
}

