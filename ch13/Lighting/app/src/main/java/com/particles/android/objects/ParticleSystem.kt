package com.particles.android.objects

import android.graphics.Color
import android.opengl.GLES20.*
import com.particles.android.Constants.Companion.BYTES_PER_FLOAT
import com.particles.android.data.VertexArray
import com.particles.android.programs.ParticleShaderProgram
import com.particles.android.util.Geometry


class ParticleSystem(private val maxParticleCount: Int) {
    private val POSITION_COMPONENT_COUNT = 3
    private val COLOR_COMPONENT_COUNT = 3
    private val VECTOR_COMPONENT_COUNT = 3
    private val PARTICLE_START_TIME_COMPONENT_COUNT = 1

    private val TOTAL_COMPONENT_COUNT = (POSITION_COMPONENT_COUNT
            + COLOR_COMPONENT_COUNT
            + VECTOR_COMPONENT_COUNT
            + PARTICLE_START_TIME_COMPONENT_COUNT)

    private val STRIDE: Int = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT

    private val particles = FloatArray(maxParticleCount * TOTAL_COMPONENT_COUNT)
    private val vertexArray = VertexArray(particles)

    private var currentParticleCount = 0
    private var nextParticle = 0

    fun addParticle(
        position: Geometry.Point, color: Int, direction: Geometry.Vector,
        particleStartTime: Float
    ) {
        val particleOffset = nextParticle * TOTAL_COMPONENT_COUNT

        var currentOffset = particleOffset
        nextParticle++

        if (currentParticleCount < maxParticleCount) {
            currentParticleCount++
        }

        if (nextParticle == maxParticleCount) {
            // Start over at the beginning, but keep currentParticleCount so
            // that all the other particles still get drawn.
            nextParticle = 0
        }

        particles[currentOffset++] = position.x;
        particles[currentOffset++] = position.y;
        particles[currentOffset++] = position.z;

        particles[currentOffset++] = Color.red(color) / 255f;
        particles[currentOffset++] = Color.green(color) / 255f;
        particles[currentOffset++] = Color.blue(color) / 255f;

        particles[currentOffset++] = direction.x;
        particles[currentOffset++] = direction.y;
        particles[currentOffset++] = direction.z;

        particles[currentOffset++] = particleStartTime;

        vertexArray.updateBuffer(particles, particleOffset, TOTAL_COMPONENT_COUNT);
    }

    fun bindData(particleProgram: ParticleShaderProgram) {
        var dataOffset = 0
        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT, STRIDE
        )
        dataOffset += POSITION_COMPONENT_COUNT
        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleProgram.aColorLocation, // getColorAttributeLocation(),
            COLOR_COMPONENT_COUNT, STRIDE
        )
        dataOffset += COLOR_COMPONENT_COUNT
        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleProgram.aDirectionVectorLocation,
            VECTOR_COMPONENT_COUNT, STRIDE
        )
        dataOffset += VECTOR_COMPONENT_COUNT
        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleProgram.aParticleStartTimeLocation,
            PARTICLE_START_TIME_COMPONENT_COUNT, STRIDE
        )
    }

    fun draw() {
        glDrawArrays(GL_POINTS, 0, currentParticleCount)
    }

}