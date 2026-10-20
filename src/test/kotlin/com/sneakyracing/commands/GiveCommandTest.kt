package com.sneakyracing.commands

import com.sneakyracing.SneakyRacing
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class GiveCommandTest {

    private lateinit var server: ServerMock
    private lateinit var plugin: SneakyRacing

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(SneakyRacing::class.java)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun giveRingRequiresPermission() {
        val player = server.addPlayer()
        server.dispatchCommand(player, "sneakyracing give ring checkpoint")
        assertEquals(0, player.inventory.contents.count { it != null && !it.isEmpty })
    }

    @Test
    fun giveRingWithPermissionAddsPlaceableItem() {
        val player = server.addPlayer()
        player.addAttachment(plugin, "sneakyracing.command.give", true)

        assertTrue(server.dispatchCommand(player, "sneakyracing give ring checkpoint"))

        val found =
            player.inventory.contents.filterNotNull().filter { !it.isEmpty }.any {
                plugin.items.isPlaceableRing(it) && plugin.items.ringTypeId(it) == "checkpoint"
            }
        assertTrue(found)
    }

    @Test
    fun giveGogglesWithPermission() {
        val player = server.addPlayer()
        player.addAttachment(plugin, "sneakyracing.command.give", true)

        assertTrue(server.dispatchCommand(player, "sneakyracing give goggles middle"))

        val found =
            player.inventory.contents.filterNotNull().filter { !it.isEmpty }.any {
                plugin.items.isRacingGoggles(it)
            }
        assertTrue(found)
    }
}
