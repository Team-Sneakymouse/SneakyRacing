package com.sneakyracing.service

import com.sneakyracing.util.Items
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Per-player ring visibility: shown only while wearing racing goggles;
 * temporarily hidden after a cross until the type's respawn timer elapses.
 */
class VisibilityService(
    private val plugin: Plugin,
    private val items: Items,
    private val tracker: RingTracker,
) {
    private data class HideKey(val playerId: UUID, val ringId: UUID)

    private val hiddenUntil = ConcurrentHashMap<HideKey, Long>()
    private val respawnTasks = ConcurrentHashMap<HideKey, BukkitTask>()
    private val placePreviewTasks = ConcurrentHashMap<UUID, BukkitTask>()

    fun wearingGoggles(player: Player): Boolean = items.isRacingGoggles(player.inventory.helmet)

    fun syncPlayer(player: Player) {
        if (!wearingGoggles(player)) {
            hideAllFor(player)
            return
        }
        val now = System.currentTimeMillis()
        for (ring in tracker.all()) {
            val key = HideKey(player.uniqueId, ring.uniqueId)
            val until = hiddenUntil[key]
            if (until != null && until > now) {
                player.hideEntity(plugin, ring)
            } else {
                hiddenUntil.remove(key)
                player.showEntity(plugin, ring)
            }
        }
    }

    fun syncAllOnline() {
        for (player in Bukkit.getOnlinePlayers()) {
            syncPlayer(player)
        }
    }

    fun onRingAdded(ring: ItemDisplay) {
        for (player in Bukkit.getOnlinePlayers()) {
            if (wearingGoggles(player) && !isHidden(player.uniqueId, ring.uniqueId)) {
                player.showEntity(plugin, ring)
            } else {
                player.hideEntity(plugin, ring)
            }
        }
    }

    /** Show a freshly placed ring to everyone for 2 seconds, then apply goggles rules. */
    fun onRingPlaced(ring: ItemDisplay) {
        placePreviewTasks.remove(ring.uniqueId)?.cancel()
        for (player in Bukkit.getOnlinePlayers()) {
            player.showEntity(plugin, ring)
        }
        placePreviewTasks[ring.uniqueId] =
            Bukkit.getScheduler().runTaskLater(
                plugin,
                Runnable {
                    placePreviewTasks.remove(ring.uniqueId)
                    if (!ring.isValid) return@Runnable
                    onRingAdded(ring)
                },
                PLACE_PREVIEW_TICKS,
            )
    }

    fun onRingRemoved(ringId: UUID) {
        placePreviewTasks.remove(ringId)?.cancel()
        for (player in Bukkit.getOnlinePlayers()) {
            val key = HideKey(player.uniqueId, ringId)
            respawnTasks.remove(key)?.cancel()
            hiddenUntil.remove(key)
        }
        // Also clear entries for offline players
        hiddenUntil.keys.filter { it.ringId == ringId }.forEach { hiddenUntil.remove(it) }
        respawnTasks.keys.filter { it.ringId == ringId }.forEach {
            respawnTasks.remove(it)?.cancel()
        }
    }

    fun hideAllFor(player: Player) {
        for (ring in tracker.all()) {
            player.hideEntity(plugin, ring)
        }
    }

    fun isHidden(playerId: UUID, ringId: UUID): Boolean {
        val until = hiddenUntil[HideKey(playerId, ringId)] ?: return false
        if (until <= System.currentTimeMillis()) {
            hiddenUntil.remove(HideKey(playerId, ringId))
            return false
        }
        return true
    }

    fun hideAfterCross(player: Player, ring: ItemDisplay, respawnSeconds: Double) {
        val key = HideKey(player.uniqueId, ring.uniqueId)
        val delayMs = (respawnSeconds.coerceAtLeast(0.0) * 1000.0).toLong()
        val until = System.currentTimeMillis() + delayMs
        hiddenUntil[key] = until
        player.hideEntity(plugin, ring)

        respawnTasks.remove(key)?.cancel()
        if (delayMs <= 0L) {
            revealIfEligible(player, ring)
            return
        }
        val ticks = (delayMs / 50L).coerceAtLeast(1L)
        respawnTasks[key] =
            Bukkit.getScheduler().runTaskLater(
                plugin,
                Runnable {
                    respawnTasks.remove(key)
                    hiddenUntil.remove(key)
                    val online = Bukkit.getPlayer(player.uniqueId) ?: return@Runnable
                    revealIfEligible(online, ring)
                },
                ticks,
            )
    }

    private fun revealIfEligible(player: Player, ring: ItemDisplay) {
        if (!ring.isValid) return
        if (wearingGoggles(player)) {
            player.showEntity(plugin, ring)
        }
    }

    fun clearPlayer(playerId: UUID) {
        hiddenUntil.keys.filter { it.playerId == playerId }.forEach { hiddenUntil.remove(it) }
        respawnTasks.keys.filter { it.playerId == playerId }.forEach {
            respawnTasks.remove(it)?.cancel()
        }
    }

    fun shutdown() {
        placePreviewTasks.values.forEach { it.cancel() }
        placePreviewTasks.clear()
        respawnTasks.values.forEach { it.cancel() }
        respawnTasks.clear()
        hiddenUntil.clear()
    }

    companion object {
        const val PLACE_PREVIEW_TICKS = 40L // 2 seconds
    }
}
