package com.sneakyracing.service

import com.sneakyracing.util.Items
import com.sneakyracing.util.Keys
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.ItemDisplay
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.util.Transformation
import org.joml.Vector3f
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Tracks racing-ring ItemDisplays in loaded chunks. */
class RingTracker(
    private val plugin: Plugin,
    private val keys: Keys,
    private val items: Items,
    private val configProvider: () -> RingConfig,
) {
    companion object {
        /** Default reach for admin set-type near the player. */
        const val NEARBY_TYPE_RADIUS = 5.0
    }

    private val rings = ConcurrentHashMap<UUID, ItemDisplay>()

    fun all(): Collection<ItemDisplay> = rings.values.filter { it.isValid }

    /** Valid rings as a list (one allocation); prefer once per tick over per-player [all]. */
    fun allSnapshot(): List<ItemDisplay> = rings.values.filter { it.isValid }

    fun get(uuid: UUID): ItemDisplay? = rings[uuid]?.takeIf { it.isValid }

    fun register(display: ItemDisplay) {
        rings[display.uniqueId] = display
    }

    fun unregister(uuid: UUID) {
        rings.remove(uuid)
    }

    fun clear() {
        rings.clear()
    }

    fun isRing(display: ItemDisplay): Boolean =
        display.persistentDataContainer.has(keys.racingRing, PersistentDataType.BYTE)

    fun typeId(display: ItemDisplay): String? =
        display.persistentDataContainer.get(keys.ringType, PersistentDataType.STRING)

    fun isPermanent(display: ItemDisplay): Boolean =
        display.persistentDataContainer.has(keys.ringPermanent, PersistentDataType.BYTE)

    fun scanWorld(world: World) {
        for (entity in world.entities) {
            if (entity is ItemDisplay && isRing(entity)) {
                register(entity)
            }
        }
    }

    fun scanAllWorlds() {
        clear()
        for (world in Bukkit.getWorlds()) {
            scanWorld(world)
        }
    }

    /**
     * Removes rings that were not placed with the permanent permission.
     * Used on reload and disable so temporary rings do not linger.
     */
    fun removeTransient(visibility: VisibilityService): Int {
        var removed = 0
        for (display in all().toList()) {
            if (isPermanent(display)) continue
            val id = display.uniqueId
            display.remove()
            unregister(id)
            visibility.onRingRemoved(id)
            removed++
        }
        return removed
    }

    fun spawnRing(
        location: org.bukkit.Location,
        type: RingType,
        permanent: Boolean = false,
    ): ItemDisplay {
        val world = requireNotNull(location.world) { "Ring spawn location has no world" }
        val display =
            world.spawn(location, ItemDisplay::class.java) { entity ->
                entity.isPersistent = permanent
                entity.setVisibleByDefault(false)
                entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE)
                entity.setItemStack(items.tintedRingStack(type))
                applyScale(entity, type.scale)
                entity.persistentDataContainer.set(keys.racingRing, PersistentDataType.BYTE, 1)
                entity.persistentDataContainer.set(keys.ringType, PersistentDataType.STRING, type.id)
                if (permanent) {
                    entity.persistentDataContainer.set(keys.ringPermanent, PersistentDataType.BYTE, 1)
                }
            }
        // ItemDisplay spawn often ignores Location yaw/pitch — set explicitly.
        display.setRotation(location.yaw, location.pitch)
        register(display)
        return display
    }

    /** Refreshes tint/item from live config. Does not change display scale (external edits stay). */
    fun refreshVisuals() {
        val config = configProvider()
        for (display in all()) {
            val id = typeId(display) ?: continue
            val type = config.type(id) ?: continue
            display.setItemStack(items.tintedRingStack(type))
        }
    }

    /** Same world, distance ≤ [radius] from [origin]. */
    fun ringsWithin(origin: Location, radius: Double = NEARBY_TYPE_RADIUS): List<ItemDisplay> {
        val world = origin.world ?: return emptyList()
        val r2 = radius * radius
        return allSnapshot().filter { ring ->
            ring.world == world && ring.location.distanceSquared(origin) <= r2
        }
    }

    /**
     * Changes ring type id, tint item, and resets display scale to [type]'s default.
     * Unlike [refreshVisuals], scale is intentionally overwritten.
     */
    fun setType(display: ItemDisplay, type: RingType) {
        display.persistentDataContainer.set(keys.ringType, PersistentDataType.STRING, type.id)
        display.setItemStack(items.tintedRingStack(type))
        applyScale(display, type.scale)
    }

    /** Applies [setType] to every tracked ring within [radius] of [origin]. Returns how many updated. */
    fun setTypeNearby(
        origin: Location,
        type: RingType,
        radius: Double = NEARBY_TYPE_RADIUS,
    ): Int {
        var updated = 0
        for (ring in ringsWithin(origin, radius)) {
            setType(ring, type)
            updated++
        }
        return updated
    }

    fun displayScale(display: ItemDisplay): Double {
        val scale = display.transformation.scale
        return ((scale.x + scale.y + scale.z) / 3.0).coerceAtLeast(0.01)
    }

    private fun applyScale(display: ItemDisplay, scale: Float) {
        val current = display.transformation
        display.transformation =
            Transformation(
                current.translation,
                current.leftRotation,
                Vector3f(scale, scale, scale),
                current.rightRotation,
            )
    }
}
