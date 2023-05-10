package com.particles.android.objects

import android.R.color
import com.particles.android.util.Geometry


class ParticleShooter(private val position: Geometry.Point, private val direction: Geometry.Vector, private val color: Int) {
    fun addParticles(
        particleSystem: ParticleSystem, currentTime: Float,
        count: Int
    ) {
        for (i in 0 until count) {
            particleSystem.addParticle(position, color, direction, currentTime)
        }
    }
}