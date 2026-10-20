package com.sneakyracing.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RingPlaneMathTest {

    @Test
    fun radiusIsHalfOfScaleTimesDiameterAndDetection() {
        assertEquals(0.5, RingPlaneMath.radius(1.0, 1.0), 1e-9)
        assertEquals(1.5, RingPlaneMath.radius(2.0, 1.5), 1e-9)
        assertEquals(0.375, RingPlaneMath.radius(1.0, 1.0, detection = 0.75), 1e-9)
    }

    @Test
    fun planeNormalForZeroYawPitchPointsSouth() {
        val (x, y, z) = RingPlaneMath.planeNormal(0.0, 0.0)
        assertEquals(0.0, x, 1e-9)
        assertEquals(0.0, y, 1e-9)
        assertEquals(1.0, z, 1e-9)
    }

    @Test
    fun planeNormalPitchDownPointsDown() {
        val (x, y, z) = RingPlaneMath.planeNormal(yawDegrees = 0.0, pitchDegrees = 90.0)
        assertEquals(0.0, x, 1e-9)
        assertEquals(-1.0, y, 1e-9)
        assertEquals(0.0, z, 1e-9)
    }

    @Test
    fun pitchedPlaneHitAlongLookAxis() {
        // Ring flat (pitch 90, normal −Y). Segment crosses vertically through center.
        val (nx, ny, nz) = RingPlaneMath.planeNormal(0.0, 90.0)
        val hit =
            RingPlaneMath.segmentIntersectsDisk(
                ax = 0.0,
                ay = 1.0,
                az = 0.0,
                bx = 0.0,
                by = -1.0,
                bz = 0.0,
                cx = 0.0,
                cy = 0.0,
                cz = 0.0,
                nx = nx,
                ny = ny,
                nz = nz,
                radius = 0.5,
            )
        assertTrue(hit)
    }

    @Test
    fun segmentCrossingDiskCenterCountsAsHit() {
        // Plane at origin facing +Z (yaw 0). Segment along Z through center.
        val hit =
            RingPlaneMath.segmentIntersectsDisk(
                ax = 0.0,
                ay = 0.0,
                az = -1.0,
                bx = 0.0,
                by = 0.0,
                bz = 1.0,
                cx = 0.0,
                cy = 0.0,
                cz = 0.0,
                nx = 0.0,
                ny = 0.0,
                nz = 1.0,
                radius = 0.5,
            )
        assertTrue(hit)
    }

    @Test
    fun segmentCrossingOutsideRadiusMisses() {
        val hit =
            RingPlaneMath.segmentIntersectsDisk(
                ax = 2.0,
                ay = 0.0,
                az = -1.0,
                bx = 2.0,
                by = 0.0,
                bz = 1.0,
                cx = 0.0,
                cy = 0.0,
                cz = 0.0,
                nx = 0.0,
                ny = 0.0,
                nz = 1.0,
                radius = 0.5,
            )
        assertFalse(hit)
    }

    @Test
    fun parallelSegmentDoesNotHit() {
        val hit =
            RingPlaneMath.segmentIntersectsDisk(
                ax = 0.0,
                ay = 0.0,
                az = 1.0,
                bx = 1.0,
                by = 0.0,
                bz = 1.0,
                cx = 0.0,
                cy = 0.0,
                cz = 0.0,
                nx = 0.0,
                ny = 0.0,
                nz = 1.0,
                radius = 2.0,
            )
        assertFalse(hit)
    }

    @Test
    fun yawCorrectionRotatesNormal() {
        val (x, y, z) = RingPlaneMath.planeNormal(yawDegrees = 0.0, pitchDegrees = 0.0, yawCorrectionDegrees = 90.0)
        // 90° from south → west (−X) in Bukkit: yaw increases toward west
        assertEquals(-1.0, x, 1e-9)
        assertEquals(0.0, y, 1e-9)
        assertEquals(0.0, z, 1e-9)
    }
}
