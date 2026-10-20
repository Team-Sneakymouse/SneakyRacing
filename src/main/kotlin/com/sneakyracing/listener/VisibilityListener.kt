package com.sneakyracing.listener

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.sneakyracing.service.CrossingService
import com.sneakyracing.service.VisibilityService
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.Plugin

/**
 * Syncs ring visibility when goggles may change.
 * Helmet changes are driven by [PlayerArmorChangeEvent] only — not every inventory click.
 */
class VisibilityListener(
    private val plugin: Plugin,
    private val visibility: VisibilityService,
    private val crossing: CrossingService,
) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        plugin.server.scheduler.runTaskLater(
            plugin,
            Runnable { visibility.syncPlayer(event.player) },
            1L,
        )
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        visibility.clearPlayer(event.player.uniqueId)
        crossing.clearPlayer(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onArmorChange(event: PlayerArmorChangeEvent) {
        if (event.slot != EquipmentSlot.HEAD) return
        val player = event.player
        plugin.server.scheduler.runTaskLater(
            plugin,
            Runnable {
                if (player.isOnline) visibility.syncPlayer(player)
            },
            1L,
        )
    }
}
