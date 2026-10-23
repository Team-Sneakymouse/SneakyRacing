package com.sneakyracing.listener

import com.sneakyracing.SneakyRacing
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class PlacePermanenceTest {
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

    @ParameterizedTest
    @CsvSource("true, false", "true, true", "false, false", "false, true")
    fun placementUsesTypeNotPlayerPermission(permanent: Boolean, permission: Boolean) {
        // MockBukkit lacks default display visibility. Stub only that unrelated API;
        // spawning, persistence metadata, placement, and cleanup remain real plugin code.
        val world = object : org.mockbukkit.mockbukkit.world.WorldMock() {
            override fun <T : org.bukkit.entity.Entity> spawn(
                location: org.bukkit.Location?,
                clazz: Class<T>?,
                function: java.util.function.Consumer<in T>?,
                reason: org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason,
                randomizeData: Boolean,
                callSpawnEvent: Boolean,
            ): T {
                require(clazz == ItemDisplay::class.java)
                requireNotNull(clazz)
                requireNotNull(location)
                val display = object : org.mockbukkit.mockbukkit.entity.ItemDisplayMock(server, java.util.UUID.randomUUID()) {
                    override fun setVisibleByDefault(visible: Boolean) = Unit
                }
                display.setLocation(location)
                server.registerEntity(display)
                val entity = clazz.cast(display)
                function?.accept(entity)
                return entity
            }
        }
        server.addWorld(world)
        val player = server.addPlayer()
        player.teleport(org.bukkit.Location(world, 0.0, 64.0, 0.0))
        player.addAttachment(plugin, "sneakyracing.ring.permanent", permission)
        val type = plugin.ringConfig.type("checkpoint")!!.copy(permanent = permanent)
        val item = plugin.items.createRingItem(type)
        player.inventory.setItemInMainHand(item)
        val listener = PlaceListener(plugin.items, plugin.ringTracker, plugin.visibilityService) {
            plugin.ringConfig.copy(rings = mapOf(type.id to type))
        }
        val event = PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, item, null,
            org.bukkit.block.BlockFace.SELF, EquipmentSlot.HAND)

        listener.onInteract(event)

        assertEquals(org.bukkit.event.Event.Result.DENY, event.useItemInHand())
        val ring = plugin.ringTracker.all().single()
        assertEquals(permanent, ring.isPersistent)
        assertEquals(permanent, plugin.ringTracker.isPermanent(ring))
        assertEquals(if (permanent) 0 else 1,
            plugin.ringTracker.removeTransient(plugin.visibilityService))
        assertEquals(permanent, ring.isValid)
    }
}
