package com.sneakyracing.commands

import com.sneakyracing.SneakyRacing
import org.bukkit.command.Command

/**
 * Base class for plugin commands. Sets permission to
 * `{identifier}.command.{name}` by default.
 */
abstract class CommandBase(name: String) : Command(name) {

    init {
        this.permission = "${SneakyRacing.IDENTIFIER}.command.$name"
    }
}
