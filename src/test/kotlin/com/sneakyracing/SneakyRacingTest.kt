package com.sneakyracing

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit

class SneakyRacingTest {

    @BeforeEach
    fun setUp() {
        MockBukkit.mock()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun loadsAndEnablesWithoutError() {
        val plugin = MockBukkit.load(SneakyRacing::class.java)
        assertNotNull(plugin)
        assertTrue(plugin.isEnabled)
        assertNotNull(SneakyRacing.instance)
        assertTrue(plugin.ringConfig.rings.containsKey("checkpoint"))
    }
}
