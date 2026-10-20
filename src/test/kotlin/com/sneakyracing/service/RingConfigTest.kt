package com.sneakyracing.service

import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.StringReader

class RingConfigTest {

    @Test
    fun parsesCorrectionsAndRingTypes() {
        val yaml =
            """
            corrections:
              diameter: 1.25
              detection: 0.8
              yaw: 15.0
            rings:
              checkpoint:
                display-name: "Checkpoint"
                tint: 16777215
                scale: 1.0
                respawn-seconds: 30
                commands:
                  - "say {player} hit"
              boost:
                display-name: "Boost"
                tint: 16711680
                scale: 1.5
                respawn-seconds: 10
                commands:
                  - "effect give {player} speed 5 1"
            """.trimIndent()

        val config = YamlConfiguration.loadConfiguration(StringReader(yaml))
        val parsed = RingConfig.from(config)

        assertEquals(1.25, parsed.corrections.diameter, 1e-9)
        assertEquals(0.8, parsed.corrections.detection, 1e-9)
        assertEquals(15.0, parsed.corrections.yaw, 1e-9)
        assertEquals(2, parsed.rings.size)

        val checkpoint = parsed.type("checkpoint")
        assertNotNull(checkpoint)
        assertEquals("Checkpoint", checkpoint!!.displayName)
        assertEquals(16_777_215, checkpoint.tint)
        assertEquals(1.0f, checkpoint.scale)
        assertEquals(30.0, checkpoint.respawnSeconds, 1e-9)
        assertEquals(listOf("say {player} hit"), checkpoint.commands)

        val boost = parsed.type("boost")!!
        assertEquals(16_711_680, boost.tint)
        assertEquals(1.5f, boost.scale)
    }
}
