package com.sneakyracing.commands

import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.sneakyracing.SneakyRacing
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.command.CommandSourceStackMock
import org.mockbukkit.mockbukkit.command.brigadier.PaperCommandsMock
import java.io.File

class ReloadCommandTest {

    private lateinit var server: ServerMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun reloadLiteralHasNativeCompletion() {
        MockBukkit.load(SneakyRacing::class.java)
        server.dispatchCommand(server.consoleSender, "sneakyracing")

        val dispatcher = PaperCommandsMock.INSTANCE.dispatcherInternal
        val parsed = dispatcher.parse("sneakyracing r", CommandSourceStackMock.from(server.consoleSender))
        val suggestions = dispatcher.getCompletionSuggestions(parsed).join().list.map { it.text }
        assertTrue(suggestions.contains("reload"))
    }

    @Test
    fun playerWithoutPermissionCannotReload() {
        val plugin = MockBukkit.load(SneakyRacing::class.java)
        plugin.config.set("test-value", "before")
        File(plugin.dataFolder, "config.yml").writeText("test-value: after\n")
        val player = server.addPlayer()

        server.dispatchCommand(player, "sneakyracing reload")

        assertEquals("before", plugin.config.getString("test-value"))
    }

    @Test
    fun playerWithPermissionCanReloadRepeatedly() {
        val plugin = MockBukkit.load(SneakyRacing::class.java)
        val player = server.addPlayer()
        player.addAttachment(plugin, "sneakyracing.command.reload", true)

        for (value in listOf("first", "second")) {
            File(plugin.dataFolder, "config.yml").writeText("test-value: $value\n")
            assertTrue(server.dispatchCommand(player, "sneakyracing reload"))
            assertEquals(value, plugin.config.getString("test-value"))
            assertEquals("SneakyRacing configuration reloaded.", player.nextMessage())
        }
    }

    @Test
    fun invalidArgumentsAreRejectedWithoutReloading() {
        val plugin = MockBukkit.load(SneakyRacing::class.java)
        server.dispatchCommand(server.consoleSender, "sneakyracing")
        plugin.config.set("test-value", "before")
        File(plugin.dataFolder, "config.yml").writeText("test-value: after\n")

        for (command in listOf("sneakyracing", "sneakyracing unknown", "sneakyracing reload extra")) {
            assertThrows(CommandSyntaxException::class.java) {
                PaperCommandsMock.INSTANCE.dispatcherInternal.execute(
                    command,
                    CommandSourceStackMock.from(server.consoleSender),
                )
            }
            assertEquals("before", plugin.config.getString("test-value"))
        }
    }

    @Test
    fun consoleReloadReparsesConfigFromDisk() {
        val plugin = MockBukkit.load(SneakyRacing::class.java)
        plugin.config.set("test-value", "before")
        File(plugin.dataFolder, "config.yml").writeText("test-value: after\n")

        assertTrue(server.dispatchCommand(server.consoleSender, "sneakyracing reload"))

        assertEquals("after", plugin.config.getString("test-value"))
        assertEquals("SneakyRacing configuration reloaded.", server.consoleSender.nextMessage())
        assertTrue(plugin.isEnabled)
    }
}
