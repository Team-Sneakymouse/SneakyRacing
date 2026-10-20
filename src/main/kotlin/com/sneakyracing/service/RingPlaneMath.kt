package com.sneakyracing.service

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Pure geometry for racing-ring plane crossing. */
object RingPlaneMath {

    private const val EPSILON = 1e-8

    /**
     * World-space hit radius:
     * half of (scale × diameterCorrection × detection).
     */
    fun radius(
        scale: Double,
        diameterCorrection: Double,
        detection: Double = 1.0,
    ): Double = 0.5 * scale * diameterCorrection * detection

    /**
     * Unit normal for a ring facing [yawDegrees]/[pitchDegrees] (Bukkit),
     * with [yawCorrectionDegrees] added to yaw.
     * Matches [org.bukkit.Location.getDirection]: yaw 0 = south (+Z),
     * pitch 0 = horizontal, positive pitch looks down.
     */
    fun planeNormal(
        yawDegrees: Double,
        pitchDegrees: Double,
        yawCorrectionDegrees: Double = 0.0,
    ): Triple<Double, Double, Double> {
        val yawRad = Math.toRadians(yawDegrees + yawCorrectionDegrees)
        val pitchRad = Math.toRadians(pitchDegrees)
        val cosPitch = cos(pitchRad)
        val x = -sin(yawRad) * cosPitch
        val y = -sin(pitchRad)
        val z = cos(yawRad) * cosPitch
        return Triple(x, y, z)
    }

    /**
     * True when the segment from [ax,ay,az] to [bx,by,bz] intersects the disk
     * centered at [cx,cy,cz] with unit normal [nx,ny,nz] and [radius].
     */
    fun segmentIntersectsDisk(
        ax: Double,
        ay: Double,
        az: Double,
        bx: Double,
        by: Double,
        bz: Double,
        cx: Double,
        cy: Double,
        cz: Double,
        nx: Double,
        ny: Double,
        nz: Double,
        radius: Double,
    ): Boolean {
        val dx = bx - ax
        val dy = by - ay
        val dz = bz - az
        val denom = nx * dx + ny * dy + nz * dz
        if (abs(denom) < EPSILON) return false

        val t = (nx * (cx - ax) + ny * (cy - ay) + nz * (cz - az)) / denom
        if (t < 0.0 || t > 1.0) return false

        val px = ax + t * dx
        val py = ay + t * dy
        val pz = az + t * dz
        // Distance within the plane (not chord length off the plane).
        val ox = px - cx
        val oy = py - cy
        val oz = pz - cz
        val along = nx * ox + ny * oy + nz * oz
        val rx = ox - along * nx
        val ry = oy - along * ny
        val rz = oz - along * nz
        val dist = sqrt(rx * rx + ry * ry + rz * rz)
        return dist <= radius + EPSILON
    }
}
