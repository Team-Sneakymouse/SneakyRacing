package com.sneakyracing.service

import com.sneakyracing.SneakyRacing
import org.bukkit.Location
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class RingTrackerSetTypeTest {

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
    fun setTypeUpdatesIdTintAndResetsScale() {
        val world = server.addSimpleWorld("world")
        val checkpoint = requireNotNull(plugin.ringConfig.type("checkpoint"))
        val boost = requireNotNull(plugin.ringConfig.type("boost"))

        val ring = plugin.ringTracker.spawnRing(Location(world, 0.0, 64.0, 0.0), checkpoint)
        assertEquals(checkpoint.scale.toDouble(), plugin.ringTracker.displayScale(ring), 0.001)

        // Simulate an external scale edit, then change type — scale must become boost default.
        val edited =
            org.bukkit.util.Transformation(
                ring.transformation.translation,
                ring.transformation.leftRotation,
                org.joml.Vector3f(3f, 3f, 3f),
                ring.transformation.rightRotation,
            )
        ring.transformation = edited
        assertEquals(3.0, plugin.ringTracker.displayScale(ring), 0.001)

        plugin.ringTracker.setType(ring, boost)

        assertEquals("boost", plugin.ringTracker.typeId(ring))
        assertEquals(boost.scale.toDouble(), plugin.ringTracker.displayScale(ring), 0.001)
    }

    @Test
    fun setTypeNearbyOnlyAffectsRingsWithinRadius() {
        val world = server.addSimpleWorld("world")
        val checkpoint = requireNotNull(plugin.ringConfig.type("checkpoint"))
        val boost = requireNotNull(plugin.ringConfig.type("boost"))

        val near = plugin.ringTracker.spawnRing(Location(world, 0.0, 64.0, 0.0), checkpoint)
        val far = plugin.ringTracker.spawnRing(Location(world, 20.0, 64.0, 0.0), checkpoint)

        val updated =
            plugin.ringTracker.setTypeNearby(Location(world, 0.0, 64.0, 0.0), boost, radius = 5.0)

        assertEquals(1, updated)
        assertEquals("boost", plugin.ringTracker.typeId(near))
        assertEquals("checkpoint", plugin.ringTracker.typeId(far))
        assertTrue(plugin.ringTracker.ringsWithin(Location(world, 0.0, 64.0, 0.0), 5.0).contains(near))
        assertTrue(plugin.ringTracker.ringsWithin(Location(world, 0.0, 64.0, 0.0), 5.0).none { it == far })
    }
}
