package com.sneakyracing.listener

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.sneakyracing.service.CrossingService
import com.sneakyracing.service.VisibilityService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.Plugin

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
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        scheduleSync(player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        if (event.inventory.type == InventoryType.CRAFTING ||
            event.inventory.type == InventoryType.PLAYER
        ) {
            scheduleSync(player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onSwap(event: PlayerSwapHandItemsEvent) {
        scheduleSync(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onArmorChange(event: PlayerArmorChangeEvent) {
        if (event.slot != EquipmentSlot.HEAD) return
        scheduleSync(event.player)
    }

    private fun scheduleSync(player: Player) {
        plugin.server.scheduler.runTaskLater(
            plugin,
            Runnable {
                if (player.isOnline) visibility.syncPlayer(player)
            },
            1L,
        )
    }
}
