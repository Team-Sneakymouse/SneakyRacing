package com.sneakyracing.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandRunnerTest {

    @Test
    fun substitutesAllPlaceholders() {
        val result =
            CommandRunner.substitute(
                template =
                    "say {player} in {world} at {player_x} {player_y} {player_z} ring {ring_x} {ring_y} {ring_z}",
                playerName = "Steve",
                world = "world",
                playerX = 1.5,
                playerY = 64.0,
                playerZ = -2.25,
                ringX = 10.0,
                ringY = 65.0,
                ringZ = 20.0,
            )
        assertEquals(
            "say Steve in world at 1.50 64.00 -2.25 ring 10.00 65.00 20.00",
            result,
        )
    }
}
