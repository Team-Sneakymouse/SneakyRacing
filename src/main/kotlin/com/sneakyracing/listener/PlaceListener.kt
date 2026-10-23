package com.sneakyracing.listener

import com.sneakyracing.service.RingConfig
import com.sneakyracing.service.RingTracker
import com.sneakyracing.service.VisibilityService
import com.sneakyracing.util.Items
import org.bukkit.GameMode
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class PlaceListener(
    private val items: Items,
    private val tracker: RingTracker,
    private val visibility: VisibilityService,
    private val configProvider: () -> RingConfig,
) : Listener {

    // ignoreCancelled=false so RIGHT_CLICK_AIR still works when another plugin cancels the interact.
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) {
            return
        }

        val item =
            event.item?.takeUnless { it.isEmpty }
                ?: event.player.inventory.itemInMainHand.takeUnless { it.isEmpty }
                ?: return
        if (!items.isPlaceableRing(item)) return

        val typeId = items.ringTypeId(item) ?: return
        val type = configProvider().type(typeId)
        if (type == null) {
            event.player.sendMessage("Unknown ring type: $typeId")
            event.isCancelled = true
            return
        }

        event.isCancelled = true
        event.setUseItemInHand(Event.Result.DENY)
        event.setUseInteractedBlock(Event.Result.DENY)

        val player = event.player
        val spawnAt = player.eyeLocation.clone()
        spawnAt.yaw = player.yaw
        spawnAt.pitch = ringPlacePitch(player.pitch, player.isSneaking)

        val display = tracker.spawnRing(spawnAt, type)
        visibility.onRingPlaced(display)

        if (shouldConsumeRingItem(player.gameMode)) {
            val hand = event.hand ?: EquipmentSlot.HAND
            val remaining = item.clone()
            remaining.amount = item.amount - 1
            if (remaining.amount <= 0) {
                player.inventory.setItem(hand, null)
            } else {
                player.inventory.setItem(hand, remaining)
            }
        }
    }
}

/** Creative placements keep the item; other modes consume one. */
fun shouldConsumeRingItem(gameMode: GameMode): Boolean = gameMode != GameMode.CREATIVE

/** Sneaking forces a flat ring (pitch 0); otherwise copy the player's look pitch. */
fun ringPlacePitch(playerPitch: Float, sneaking: Boolean): Float =
    if (sneaking) 0f else playerPitch
