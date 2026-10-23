package com.sneakyracing.listener

import com.sneakyracing.service.VisibilityService
import org.bukkit.GameMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlaceConsumeTest {

    @Test
    fun creativeDoesNotConsumeRingItem() {
        assertFalse(shouldConsumeRingItem(GameMode.CREATIVE))
    }

    @Test
    fun survivalConsumesRingItem() {
        assertTrue(shouldConsumeRingItem(GameMode.SURVIVAL))
        assertTrue(shouldConsumeRingItem(GameMode.ADVENTURE))
    }

    @Test
    fun placePreviewIsTwoSeconds() {
        assertEquals(40L, VisibilityService.PLACE_PREVIEW_TICKS)
    }

    @Test
    fun ringPlacePitchCopiesUnlessSneaking() {
        assertEquals(-25f, ringPlacePitch(-25f, sneaking = false))
        assertEquals(0f, ringPlacePitch(-25f, sneaking = true))
    }
}
