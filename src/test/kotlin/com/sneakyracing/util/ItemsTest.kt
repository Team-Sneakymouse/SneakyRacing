package com.sneakyracing.util

import com.sneakyracing.SneakyRacing
import com.sneakyracing.service.RingType
import io.papermc.paper.datacomponent.DataComponentTypes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit

class ItemsTest {

    private lateinit var plugin: SneakyRacing
    private lateinit var items: Items

    @BeforeEach
    fun setUp() {
        MockBukkit.mock()
        plugin = MockBukkit.load(SneakyRacing::class.java)
        items = plugin.items
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun ringItemHasTypePdcAndTint() {
        val type =
            RingType(
                id = "checkpoint",
                displayName = "Checkpoint",
                tint = 16_777_215,
                scale = 1.0f,
                respawnSeconds = 30.0,
                commands = emptyList(),
            )
        val item = items.createRingItem(type, 3)
        assertEquals(3, item.amount)
        assertTrue(items.isPlaceableRing(item))
        assertEquals("checkpoint", items.ringTypeId(item))
        assertFalse(items.isRacingGoggles(item))

        val cmd = item.getData(DataComponentTypes.CUSTOM_MODEL_DATA)
        assertNotNull(cmd)
        assertEquals(1, cmd!!.colors().size)
        assertEquals(16_777_215, cmd.colors()[0].asRGB() and 0xFFFFFF)
    }

    @Test
    fun gogglesItemHasPdcFlag() {
        val item = items.createGoggles(1)
        assertTrue(items.isRacingGoggles(item))
        assertFalse(items.isPlaceableRing(item))
    }
}
