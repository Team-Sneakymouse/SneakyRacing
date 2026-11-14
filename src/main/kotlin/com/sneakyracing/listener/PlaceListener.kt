package com.sneakyracing.listener

import com.sneakyitems.events.MagicItemClickEvent
import com.sneakyracing.service.RingConfig
import com.sneakyracing.service.RingTracker
import com.sneakyracing.service.VisibilityService
import com.sneakyracing.util.Items
import org.bukkit.GameMode
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot

class PlaceListener(
    private val items: Items,
    private val tracker: RingTracker,
    private val visibility: VisibilityService,
    private val configProvider: () -> RingConfig,
) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    fun onMagicClick(event: MagicItemClickEvent) {
        if (!event.isRightClick) return
        if (event.hand != EquipmentSlot.HAND) return
        if (event.trigger != MagicItemClickEvent.Trigger.AIR &&
            event.trigger != MagicItemClickEvent.Trigger.BLOCK
        ) {
            return
        }
        if (!items.isPlaceableRing(event.item)) return
        if (event.isAlreadyDenied) return

        val typeId = items.ringTypeId(event.item) ?: return
        val type = configProvider().type(typeId)
        if (type == null) {
            event.player.sendMessage("Unknown ring type: $typeId")
            event.isCancelled = true
            return
        }

        event.isCancelled = true
        if (event.hasInteractResults()) {
            event.setUseItemInHand(Event.Result.DENY)
            event.setUseInteractedBlock(Event.Result.DENY)
        }

        val player = event.player
        val spawnAt = player.eyeLocation.clone()
        spawnAt.yaw = player.yaw
        spawnAt.pitch = ringPlacePitch(player.pitch, player.isSneaking)

        val display = tracker.spawnRing(spawnAt, type)
        visibility.onRingPlaced(display)

        if (shouldConsumeRingItem(player.gameMode)) {
            val item = event.item
            val remaining = item.clone()
            remaining.amount = item.amount - 1
            if (remaining.amount <= 0) {
                player.inventory.setItem(event.hand, null)
            } else {
                player.inventory.setItem(event.hand, remaining)
            }
        }
    }
}

/** Creative placements keep the item; other modes consume one. */
fun shouldConsumeRingItem(gameMode: GameMode): Boolean = gameMode != GameMode.CREATIVE

/** Sneaking forces a flat ring (pitch 0); otherwise copy the player's look pitch. */
fun ringPlacePitch(playerPitch: Float, sneaking: Boolean): Float =
    if (sneaking) 0f else playerPitch
