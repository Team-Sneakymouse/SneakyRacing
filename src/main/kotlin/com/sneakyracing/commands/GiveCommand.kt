package com.sneakyracing.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.sneakyracing.SneakyRacing
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

class GiveCommand(private val plugin: SneakyRacing) {

    fun subtree(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("give")
            .requires { it.sender.hasPermission("${SneakyRacing.IDENTIFIER}.command.give") }
            .then(
                Commands.literal("ring")
                    .then(
                        Commands.argument("type", StringArgumentType.word())
                            .suggests { _, builder -> suggestRingTypes(builder) }
                            .executes { giveRing(it, amount = 1, targetOverride = null) }
                            .then(
                                Commands.argument("player", ArgumentTypes.player())
                                    .executes { giveRing(it, amount = 1, targetOverride = resolvePlayer(it)) }
                                    .then(
                                        Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                            .executes {
                                                giveRing(
                                                    it,
                                                    amount = IntegerArgumentType.getInteger(it, "amount"),
                                                    targetOverride = resolvePlayer(it),
                                                )
                                            },
                                    ),
                            )
                            .then(
                                Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                    .executes {
                                        giveRing(
                                            it,
                                            amount = IntegerArgumentType.getInteger(it, "amount"),
                                            targetOverride = null,
                                        )
                                    },
                            ),
                    ),
            )
            .then(
                Commands.literal("goggles")
                    .executes { giveGoggles(it, amount = 1, targetOverride = null) }
                    .then(
                        Commands.argument("player", ArgumentTypes.player())
                            .executes { giveGoggles(it, amount = 1, targetOverride = resolvePlayer(it)) }
                            .then(
                                Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                    .executes {
                                        giveGoggles(
                                            it,
                                            amount = IntegerArgumentType.getInteger(it, "amount"),
                                            targetOverride = resolvePlayer(it),
                                        )
                                    },
                            ),
                    )
                    .then(
                        Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                            .executes {
                                giveGoggles(
                                    it,
                                    amount = IntegerArgumentType.getInteger(it, "amount"),
                                    targetOverride = null,
                                )
                            },
                    ),
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

    private fun resolvePlayer(context: CommandContext<CommandSourceStack>): Player {
        val resolver = context.getArgument("player", PlayerSelectorArgumentResolver::class.java)
        return resolver.resolve(context.source).first()
    }

    private fun giveRing(
        context: CommandContext<CommandSourceStack>,
        amount: Int,
        targetOverride: Player?,
    ): Int {
        val typeId = StringArgumentType.getString(context, "type")
        val type = plugin.ringConfig.type(typeId)
        if (type == null) {
            context.source.sender.sendMessage("Unknown ring type: $typeId")
            return 0
        }
        val target = targetOverride ?: (context.source.sender as? Player)
        if (target == null) {
            context.source.sender.sendMessage("Specify a player when running from console.")
            return 0
        }
        val given = plugin.items.createRingItem(type, amount)
        val leftover = target.inventory.addItem(given)
        if (leftover.isNotEmpty()) {
            leftover.values.forEach { target.world.dropItemNaturally(target.location, it) }
        }
        context.source.sender.sendMessage("Gave ${amount}× ${type.displayName} ring to ${target.name}.")
        return 1
    }

    private fun giveGoggles(
        context: CommandContext<CommandSourceStack>,
        amount: Int,
        targetOverride: Player?,
    ): Int {
        val target = targetOverride ?: (context.source.sender as? Player)
        if (target == null) {
            context.source.sender.sendMessage("Specify a player when running from console.")
            return 0
        }
        val given = plugin.items.createGoggles(amount)
        val leftover = target.inventory.addItem(given)
        if (leftover.isNotEmpty()) {
            leftover.values.forEach { target.world.dropItemNaturally(target.location, it) }
        }
        context.source.sender.sendMessage("Gave ${amount}× racing goggles to ${target.name}.")
        return 1
    }
}
