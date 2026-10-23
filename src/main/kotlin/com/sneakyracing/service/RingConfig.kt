package com.sneakyracing.service

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration

data class Corrections(
    val diameter: Double,
    /** Multiplier on hit-disk radius; lower = stricter crossing. */
    val detection: Double,
    val yaw: Double,
)

data class RingType(
    val id: String,
    val displayName: String,
    val tint: Int,
    val scale: Float,
    val respawnSeconds: Double,
    val commands: List<String>,
    val permanent: Boolean = true,
)

data class RingConfig(
    val corrections: Corrections,
    val rings: Map<String, RingType>,
) {
    fun type(id: String): RingType? = rings[id]

    companion object {
        fun from(config: FileConfiguration): RingConfig {
            val correctionsSection = config.getConfigurationSection("corrections")
            val corrections =
                Corrections(
                    diameter = correctionsSection?.getDouble("diameter", 1.0) ?: 1.0,
                    detection = correctionsSection?.getDouble("detection", 0.75) ?: 0.75,
                    yaw = correctionsSection?.getDouble("yaw", 0.0) ?: 0.0,
                )
            val ringsSection = config.getConfigurationSection("rings")
            val rings = linkedMapOf<String, RingType>()
            if (ringsSection != null) {
                for (id in ringsSection.getKeys(false)) {
                    val section = ringsSection.getConfigurationSection(id) ?: continue
                    rings[id] = parseRingType(id, section)
                }
            }
            return RingConfig(corrections, rings)
        }

        private fun parseRingType(id: String, section: ConfigurationSection): RingType =
            RingType(
                id = id,
                displayName = section.getString("display-name", id) ?: id,
                tint = section.getInt("tint", 16_777_215),
                scale = section.getDouble("scale", 1.0).toFloat(),
                respawnSeconds = section.getDouble("respawn-seconds", 30.0),
                commands = section.getStringList("commands"),
                permanent = section.getBoolean("permanent", true),
            )
    }
}
