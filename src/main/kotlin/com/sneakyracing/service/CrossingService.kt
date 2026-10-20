package com.sneakyracing.service

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

/** Periodically checks whether goggles-wearing players crossed a ring disk. */
class CrossingService(
    private val plugin: Plugin,
    private val tracker: RingTracker,
    private val visibility: VisibilityService,
    private val configProvider: () -> RingConfig,
) {
    private val lastBody = ConcurrentHashMap<UUID, Location>()
    private var task: BukkitTask? = null

    fun start() {
        stop()
        task =
            plugin.server.scheduler.runTaskTimer(
                plugin,
                Runnable { tick() },
                1L,
                1L,
            )
    }

    fun stop() {
        task?.cancel()
        task = null
        lastBody.clear()
    }

    fun clearPlayer(playerId: UUID) {
        lastBody.remove(playerId)
    }

    private fun tick() {
        val config = configProvider()
        for (player in plugin.server.onlinePlayers) {
            if (!canTriggerRing(player.gameMode) || !visibility.wearingGoggles(player)) {
                lastBody.remove(player.uniqueId)
                continue
            }
            val current = bodyPoint(player)
            val previous = lastBody.put(player.uniqueId, current.clone())
            if (previous == null || previous.world != current.world) continue

            for (ring in tracker.all()) {
                if (ring.world != current.world) continue
                if (visibility.isHidden(player.uniqueId, ring.uniqueId)) continue

                val typeId = tracker.typeId(ring) ?: continue
                val type = config.type(typeId) ?: continue

                val scale = tracker.displayScale(ring)
                val radius =
                    RingPlaneMath.radius(
                        scale,
                        config.corrections.diameter,
                        config.corrections.detection,
                    )
                // Skip far rings cheaply
                val ringLoc = ring.location
                val reach = max(radius * 2.0, 4.0) + player.velocity.length() * 2.0
                if (ringLoc.distanceSquared(current) > reach * reach &&
                    ringLoc.distanceSquared(previous) > reach * reach
                ) {
                    continue
                }

                val (nx, ny, nz) =
                    RingPlaneMath.planeNormal(
                        ring.yaw.toDouble(),
                        ring.pitch.toDouble(),
                        config.corrections.yaw,
                    )
                val hit =
                    RingPlaneMath.segmentIntersectsDisk(
                        previous.x,
                        previous.y,
                        previous.z,
                        current.x,
                        current.y,
                        current.z,
                        ringLoc.x,
                        ringLoc.y,
                        ringLoc.z,
                        nx,
                        ny,
                        nz,
                        radius,
                    )
                if (!hit) continue

                visibility.hideAfterCross(player, ring, type.respawnSeconds)
                CommandRunner.run(type.commands, player, current, ringLoc)
            }
        }
    }

    /** Approximate body center used for plane crossing. */
    private fun bodyPoint(player: Player): Location {
        val loc = player.location.clone()
        loc.y += player.height * 0.5
        return loc
    }
}

/** Creative and spectator players do not trigger rings. */
fun canTriggerRing(gameMode: GameMode): Boolean =
    gameMode != GameMode.CREATIVE && gameMode != GameMode.SPECTATOR

