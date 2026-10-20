package com.sneakyracing.util

import org.bukkit.NamespacedKey

enum class GogglesHeight(
    val id: String,
    val displayLabel: String,
    val itemModel: NamespacedKey,
) {
    HIGH(
        id = "high",
        displayLabel = "High",
        itemModel = NamespacedKey("lom", "racing_goggles_high"),
    ),
    MIDDLE(
        id = "middle",
        displayLabel = "Middle",
        itemModel = NamespacedKey("lom", "racing_goggles_middle"),
    ),
    LOW(
        id = "low",
        displayLabel = "Low",
        itemModel = NamespacedKey("lom", "racing_goggles_low"),
    );

    companion object {
        fun fromId(raw: String): GogglesHeight? =
            entries.firstOrNull { it.id.equals(raw, ignoreCase = true) }

        val itemModels: Set<NamespacedKey> = entries.map { it.itemModel }.toSet()
    }
}
