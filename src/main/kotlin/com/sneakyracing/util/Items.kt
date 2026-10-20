package com.sneakyracing.util

import com.sneakyracing.service.RingType
import com.sneakyutils.util.TextUtility
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.Equippable
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class Items(private val keys: Keys) {

    fun createRingItem(type: RingType, amount: Int = 1): ItemStack {
        val item = ItemStack(Material.PAPER, amount.coerceAtLeast(1))
        val meta = item.itemMeta
        meta.itemModel = RING_MODEL
        meta.displayName(TextUtility.convertToComponent("&a${type.displayName}"))
        meta.persistentDataContainer.set(keys.placeableRing, PersistentDataType.BYTE, 1)
        meta.persistentDataContainer.set(keys.ringType, PersistentDataType.STRING, type.id)
        item.itemMeta = meta
        applyTint(item, type.tint)
        return item
    }

    fun createGoggles(height: GogglesHeight, amount: Int = 1): ItemStack {
        val item = ItemStack(Material.PAPER, amount.coerceAtLeast(1))
        val meta = item.itemMeta
        meta.itemModel = height.itemModel
        meta.displayName(TextUtility.convertToComponent("&aRacing Goggles (${height.displayLabel})"))
        meta.lore(listOf(TextUtility.convertToComponent("&7Wear to see racing rings.")))
        meta.persistentDataContainer.set(keys.racingGoggles, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        item.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HEAD).build())
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
        val meta = item.itemMeta
        if (meta.persistentDataContainer.has(keys.racingGoggles, PersistentDataType.BYTE)) return true
        val model = meta.itemModel ?: return false
        return model in GogglesHeight.itemModels
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
