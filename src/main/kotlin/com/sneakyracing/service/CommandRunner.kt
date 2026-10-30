package com.sneakyracing.service

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Substitutes placeholders and dispatches ring-hit commands as console.
 * Only the placeholders below may be substituted; never put user-supplied text into the command string.
 */
object CommandRunner {

    fun run(commands: List<String>, player: Player, crossAt: Location, ringAt: Location) {
        if (commands.isEmpty()) return
        val replaced =
            commands.map { template ->
                template
                    .replace("{player}", player.name)
                    .replace("{world}", crossAt.world?.name ?: "")
                    .replace("{player_x}", format(crossAt.x))
                    .replace("{player_y}", format(crossAt.y))
                    .replace("{player_z}", format(crossAt.z))
                    .replace("{ring_x}", format(ringAt.x))
                    .replace("{ring_y}", format(ringAt.y))
                    .replace("{ring_z}", format(ringAt.z))
            }
        for (command in replaced) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        }
    }

    fun substitute(
        template: String,
        playerName: String,
        world: String,
        playerX: Double,
        playerY: Double,
        playerZ: Double,
        ringX: Double,
        ringY: Double,
        ringZ: Double,
    ): String =
        template
            .replace("{player}", playerName)
            .replace("{world}", world)
            .replace("{player_x}", format(playerX))
            .replace("{player_y}", format(playerY))
            .replace("{player_z}", format(playerZ))
            .replace("{ring_x}", format(ringX))
            .replace("{ring_y}", format(ringY))
            .replace("{ring_z}", format(ringZ))

    private fun format(value: Double): String = String.format("%.2f", value)
}
