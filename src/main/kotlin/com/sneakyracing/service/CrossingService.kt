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
    private val lastBody = ConcurrentHashMap<UUID, BodySample>()
    private var task: BukkitTask? = null

    fun start() {
        stop()
        task =
            plugin.server.scheduler.runTaskTimer(
                plugin,
                Runnable { tick() },
                TICK_PERIOD,
                TICK_PERIOD,
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
        val rings = tracker.allSnapshot()
        if (rings.isEmpty()) {
            lastBody.clear()
            return
        }

        for (player in plugin.server.onlinePlayers) {
            if (!canTriggerRing(player.gameMode) || !visibility.wearingGoggles(player)) {
                lastBody.remove(player.uniqueId)
                continue
            }
            val current = bodyPoint(player)
            val previous = lastBody.put(player.uniqueId, current)
            if (previous == null || previous.worldUuid != current.worldUuid) continue

            val speedBoost = player.velocity.length() * 2.0
            for (ring in rings) {
                if (ring.world != player.world) continue
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
                val ringLoc = ring.location
                val reach = max(radius * 2.0, 4.0) + speedBoost
                val reachSq = reach * reach
                if (ringLoc.distanceSquared(current.x, current.y, current.z) > reachSq &&
                    ringLoc.distanceSquared(previous.x, previous.y, previous.z) > reachSq
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
                CommandRunner.run(
                    type.commands,
                    player,
                    Location(player.world, current.x, current.y, current.z),
                    ringLoc,
                )
            }
        }
    }

    /** Body-center sample without allocating a Location each tick. */
    private fun bodyPoint(player: Player): BodySample {
        val loc = player.location
        return BodySample(
            worldUuid = loc.world?.uid,
            x = loc.x,
            y = loc.y + player.height * 0.5,
            z = loc.z,
        )
    }

    private data class BodySample(
        val worldUuid: UUID?,
        val x: Double,
        val y: Double,
        val z: Double,
    )

    companion object {
        /** Every 2 ticks: halves cadence with acceptable precision for racing speeds. */
        const val TICK_PERIOD = 2L
    }
}

private fun Location.distanceSquared(x: Double, y: Double, z: Double): Double {
    val dx = this.x - x
    val dy = this.y - y
    val dz = this.z - z
    return dx * dx + dy * dy + dz * dz
}

/** Creative and spectator players do not trigger rings. */
fun canTriggerRing(gameMode: GameMode): Boolean =
    gameMode != GameMode.CREATIVE && gameMode != GameMode.SPECTATOR

