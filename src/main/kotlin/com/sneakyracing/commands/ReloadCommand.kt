package com.sneakyracing.commands

import com.sneakyracing.SneakyRacing
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

class ReloadCommand(private val plugin: SneakyRacing) {

    fun create(): LiteralCommandNode<CommandSourceStack> =
        Commands.literal(SneakyRacing.IDENTIFIER)
            .then(
                Commands.literal("reload")
                    .requires {
                        it.sender.hasPermission("${SneakyRacing.IDENTIFIER}.command.reload")
                    }
                    .executes { context ->
                        plugin.reloadPlugin()
                        context.source.sender.sendMessage("${plugin.name} configuration reloaded.")
                        1
                    },
            )
            .then(GiveCommand(plugin).subtree())
            .then(SetTypeCommand(plugin).subtree())
            .build()
}
