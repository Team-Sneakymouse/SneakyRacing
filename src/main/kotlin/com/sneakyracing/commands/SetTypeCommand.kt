package com.sneakyracing.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.sneakyracing.SneakyRacing
import com.sneakyracing.service.RingTracker
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

class SetTypeCommand(private val plugin: SneakyRacing) {

    fun subtree(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("settype")
            .requires { it.sender.hasPermission("${SneakyRacing.IDENTIFIER}.command.settype") }
            .then(
                Commands.argument("type", StringArgumentType.word())
                    .suggests { _, builder -> suggestRingTypes(builder) }
                    .executes { context ->
                        val sender = context.source.sender
                        val player = sender as? Player
                        if (player == null) {
                            sender.sendMessage("Only players can set nearby ring types.")
                            return@executes 0
                        }
                        val typeId = StringArgumentType.getString(context, "type")
                        val type = plugin.ringConfig.type(typeId)
                        if (type == null) {
                            sender.sendMessage("Unknown ring type: $typeId")
                            return@executes 0
                        }
                        val updated =
                            plugin.ringTracker.setTypeNearby(
                                player.location,
                                type,
                                RingTracker.NEARBY_TYPE_RADIUS,
                            )
                        sender.sendMessage(
                            "Set $updated ring(s) within ${RingTracker.NEARBY_TYPE_RADIUS} blocks to ${type.displayName}.",
                        )
                        1
                    },
            )

    private fun suggestRingTypes(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val remaining = builder.remainingLowerCase
        for (id in plugin.ringConfig.rings.keys) {
            if (id.lowercase().startsWith(remaining)) {
                builder.suggest(id)
            }
        }
        return builder.buildFuture()
    }
}
