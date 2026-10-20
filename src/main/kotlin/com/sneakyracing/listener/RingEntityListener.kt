package com.sneakyracing.listener

import com.sneakyracing.service.RingTracker
import com.sneakyracing.service.VisibilityService
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.EntitiesLoadEvent

class RingEntityListener(
    private val tracker: RingTracker,
    private val visibility: VisibilityService,
) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onChunkLoad(event: ChunkLoadEvent) {
        for (entity in event.chunk.entities) {
            if (entity is ItemDisplay && tracker.isRing(entity)) {
                tracker.register(entity)
                visibility.onRingAdded(entity)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntitiesLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            if (entity is ItemDisplay && tracker.isRing(entity)) {
                tracker.register(entity)
                visibility.onRingAdded(entity)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onRemove(event: EntityRemoveEvent) {
        val entity = event.entity
        if (entity !is ItemDisplay) return
        if (!tracker.isRing(entity) && tracker.get(entity.uniqueId) == null) return
        tracker.unregister(entity.uniqueId)
        visibility.onRingRemoved(entity.uniqueId)
    }
}
