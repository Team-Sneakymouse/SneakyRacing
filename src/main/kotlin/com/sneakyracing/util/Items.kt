package com.sneakyracing.util

import com.sneakyitems.api.SneakyItemsAPI
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
        val n = amount.coerceAtLeast(1)
        val item = magicStack(RING_MAGIC, n) ?: ItemStack(Material.PAPER, n).also { stack ->
            val meta = stack.itemMeta
            meta.itemModel = RING_MODEL
            stack.itemMeta = meta
        }
        item.editMeta { meta ->
            meta.displayName(TextUtility.convertToComponent("&a${type.displayName}"))
            meta.persistentDataContainer.set(keys.ringType, PersistentDataType.STRING, type.id)
        }
        applyTint(item, type.tint)
        return item
    }

    fun createGoggles(height: GogglesHeight, amount: Int = 1): ItemStack {
        val n = amount.coerceAtLeast(1)
        val item = magicStack(GOGGLES_MAGIC, n) ?: ItemStack(Material.PAPER, n)
        item.editMeta { meta ->
            meta.itemModel = height.itemModel
            meta.displayName(TextUtility.convertToComponent("&aRacing Goggles (${height.displayLabel})"))
            meta.lore(listOf(TextUtility.convertToComponent("&7Wear to see racing rings.")))
        }
        item.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HEAD).build())
        return item
    }

    fun isPlaceableRing(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false
        if (SneakyItemsAPI.isItem(item, RING_MAGIC)) return true
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(keys.placeableRing, PersistentDataType.BYTE)
    }

    fun ringTypeId(item: ItemStack): String? {
        if (!item.hasItemMeta()) return null
        return item.itemMeta.persistentDataContainer.get(keys.ringType, PersistentDataType.STRING)
    }

    fun isRacingGoggles(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false
        if (SneakyItemsAPI.isItem(item, GOGGLES_MAGIC)) return true
        if (!item.hasItemMeta()) return false
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

    /** Display-only stack for ItemDisplay entities — not a player-held magic item. */
    fun tintedRingStack(type: RingType): ItemStack {
        val item = ItemStack(Material.PAPER)
        val meta: ItemMeta = item.itemMeta
        meta.itemModel = RING_MODEL
        item.itemMeta = meta
        applyTint(item, type.tint)
        return item
    }

    private fun magicStack(name: String, amount: Int): ItemStack? {
        val stack = SneakyItemsAPI.getItemStack(name)?.clone() ?: return null
        stack.amount = amount.coerceIn(1, stack.maxStackSize.coerceAtLeast(1))
        return stack
    }

    companion object {
        const val RING_MAGIC = "item-sneakyracing-ring"
        const val GOGGLES_MAGIC = "item-sneakyracing-goggles"
        val RING_MODEL: NamespacedKey = NamespacedKey("lom", "racing_ring")
    }
}
