package com.sneakyracing.commands

import com.sneakyracing.SneakyRacing
import org.bukkit.Location
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class SetTypeCommandTest {

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
    fun setTypeRequiresPermission() {
        val world = server.addSimpleWorld("world")
        val player = server.addPlayer()
        player.teleport(Location(world, 0.0, 64.0, 0.0))

        val checkpoint = requireNotNull(plugin.ringConfig.type("checkpoint"))
        plugin.ringTracker.spawnRing(Location(world, 1.0, 64.0, 0.0), checkpoint)

        server.dispatchCommand(player, "sneakyracing settype boost")
        assertEquals("checkpoint", plugin.ringTracker.typeId(plugin.ringTracker.allSnapshot().first()))
    }

    @Test
    fun setTypeWithPermissionChangesNearbyRing() {
        val world = server.addSimpleWorld("world")
        val player = server.addPlayer()
        player.teleport(Location(world, 0.0, 64.0, 0.0))
        player.addAttachment(plugin, "sneakyracing.command.settype", true)

        val checkpoint = requireNotNull(plugin.ringConfig.type("checkpoint"))
        val ring = plugin.ringTracker.spawnRing(Location(world, 1.0, 64.0, 0.0), checkpoint)

        assertTrue(server.dispatchCommand(player, "sneakyracing settype boost"))
        assertEquals("boost", plugin.ringTracker.typeId(ring))
        val boost = requireNotNull(plugin.ringConfig.type("boost"))
        assertEquals(boost.scale.toDouble(), plugin.ringTracker.displayScale(ring), 0.001)
    }
}
