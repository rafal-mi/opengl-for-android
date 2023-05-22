package com.particles.android.objects

import android.opengl.Matrix.multiplyMV
import android.opengl.Matrix.setRotateEulerM
import com.particles.android.util.Geometry.*
import com.particles.android.util.Geometry.Vector
import java.util.*


class ParticleShooter(
    private val position: Point,
    private val direction: Vector,
    private val color: Int,
    private val angleVarianceInDegrees: Float,
    private val speedVariance: Float
    ) {
//    private val angleVariance = 0f
//    private val speedVariance = 0f
    private val random: Random = Random()
    private val rotationMatrix = FloatArray(16)
    private val directionVector = FloatArray(4)
    private val resultVector = FloatArray(4)

    init {
        directionVector[0] = direction.x
        directionVector[1] = direction.y
        directionVector[2] = direction.z
    }

    fun addParticles(
        particleSystem: ParticleSystem, currentTime: Float,
        count: Int
    ) {
        for (i in 0 until count) {
            setRotateEulerM(
                rotationMatrix, 0,
                (random.nextFloat() - 0.5f) * angleVarianceInDegrees,
                (random.nextFloat() - 0.5f) * angleVarianceInDegrees,
                (random.nextFloat() - 0.5f) * angleVarianceInDegrees
            )

            multiplyMV(
                resultVector, 0,
                rotationMatrix, 0,
                directionVector, 0
            )

            val speedAdjustment: Float = 1f + random.nextFloat() * speedVariance

            val thisDirection = Vector(
                resultVector[0] * speedAdjustment,
                resultVector[1] * speedAdjustment,
                resultVector[2] * speedAdjustment
            )

            particleSystem.addParticle(position, color, thisDirection, currentTime)
        }
    }
}