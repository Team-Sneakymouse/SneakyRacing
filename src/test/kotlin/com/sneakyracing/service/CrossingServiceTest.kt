package com.sneakyracing.service

import org.bukkit.GameMode
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CrossingServiceTest {

    @Test
    fun creativeAndSpectatorDoNotTrigger() {
        assertFalse(canTriggerRing(GameMode.CREATIVE))
        assertFalse(canTriggerRing(GameMode.SPECTATOR))
    }

    @Test
    fun survivalAndAdventureCanTrigger() {
        assertTrue(canTriggerRing(GameMode.SURVIVAL))
        assertTrue(canTriggerRing(GameMode.ADVENTURE))
    }
}
