package com.sneakyracing.util

import com.sneakyracing.service.RingType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class Items(private val keys: Keys) {

    fun createRingItem(type: RingType, amount: Int = 1): ItemStack {
        val item = ItemStack(Material.PAPER, amount.coerceAtLeast(1))
        val meta = item.itemMeta
        meta.itemModel = RING_MODEL
        meta.displayName(
            Component.text(type.displayName)
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false),
        )
        meta.persistentDataContainer.set(keys.placeableRing, PersistentDataType.BYTE, 1)
        meta.persistentDataContainer.set(keys.ringType, PersistentDataType.STRING, type.id)
        item.itemMeta = meta
        applyTint(item, type.tint)
        return item
    }

    fun createGoggles(amount: Int = 1): ItemStack {
        val item = ItemStack(Material.LEATHER_HELMET, amount.coerceAtLeast(1))
        val meta = item.itemMeta
        meta.displayName(
            Component.text("Racing Goggles")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false),
        )
        meta.lore(
            listOf(
                Component.text("Wear to see racing rings.")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
            ),
        )
        meta.persistentDataContainer.set(keys.racingGoggles, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }

    fun isPlaceableRing(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty || !item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(keys.placeableRing, PersistentDataType.BYTE)
    }

    fun ringTypeId(item: ItemStack): String? {
        if (!item.hasItemMeta()) return null
        return item.itemMeta.persistentDataContainer.get(keys.ringType, PersistentDataType.STRING)
    }

    fun isRacingGoggles(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty || !item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(keys.racingGoggles, PersistentDataType.BYTE)
    }

    fun applyTint(item: ItemStack, rgb: Int) {
        val color = Color.fromRGB(rgb and 0xFFFFFF)
        item.setData(
            DataComponentTypes.CUSTOM_MODEL_DATA,
            CustomModelData.customModelData().addColor(color).build(),
        )
    }

    fun tintedRingStack(type: RingType): ItemStack {
        val item = ItemStack(Material.PAPER)
        val meta: ItemMeta = item.itemMeta
        meta.itemModel = RING_MODEL
        item.itemMeta = meta
        applyTint(item, type.tint)
        return item
    }

    companion object {
        val RING_MODEL: NamespacedKey = NamespacedKey("lom", "racing_ring")
    }
}
