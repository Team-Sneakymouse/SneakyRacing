package com.sneakyracing.util

import org.bukkit.NamespacedKey
import org.bukkit.plugin.Plugin

/** PDC keys for racing rings and goggles. */
class Keys(plugin: Plugin) {
    val racingRing: NamespacedKey = NamespacedKey(plugin, "racing_ring")
    val ringType: NamespacedKey = NamespacedKey(plugin, "ring_type")
    val ringPermanent: NamespacedKey = NamespacedKey(plugin, "ring_permanent")
    val placeableRing: NamespacedKey = NamespacedKey(plugin, "placeable_ring")
    val racingGoggles: NamespacedKey = NamespacedKey(plugin, "racing_goggles")
}
