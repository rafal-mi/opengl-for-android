package com.particles.android

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix.*
import com.particles.android.objects.ParticleShooter
import com.particles.android.objects.ParticleSystem
import com.particles.android.programs.ParticleShaderProgram
import com.particles.android.util.Geometry
import com.particles.android.util.Geometry.Point
import com.particles.android.util.MatrixHelper
import com.particles.android.util.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ParticlesRenderer(private val context: Context) : Renderer {
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private var particleProgram: ParticleShaderProgram? = null
    var particleSystem: ParticleSystem? = null
    var redParticleShooter: ParticleShooter? = null
    var greenParticleShooter: ParticleShooter? = null
    var blueParticleShooter: ParticleShooter? = null
    var globalStartTime: Long = 0
    private var texture: Int = 0

    val angleVarianceInDegrees = 5f
    val speedVariance = 1f

    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        // Enable additive blending
        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE)

        particleProgram = ParticleShaderProgram(context)
        particleSystem = ParticleSystem(10000)
        globalStartTime = System.nanoTime()

        val particleDirection = Geometry.Vector(0f, 0.5f, 0f)

        val angleVarianceInDegrees = 5f
        val speedVariance = 1f

        redParticleShooter = ParticleShooter(
            Point(-1f, 0f, 0f),
            particleDirection,
            Color.rgb(255, 50, 5),
            angleVarianceInDegrees,
            speedVariance
        )

        greenParticleShooter = ParticleShooter(
            Point(0f, 0f, 0f),
            particleDirection,
            Color.rgb(25, 255, 25),
            angleVarianceInDegrees,
            speedVariance
        )

        blueParticleShooter = ParticleShooter(
            Point(1f, 0f, 0f),
            particleDirection,
            Color.rgb(5, 50, 255),
            angleVarianceInDegrees,
            speedVariance
        )

        texture = TextureHelper.loadTexture(context, R.drawable.particle_texture)
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        MatrixHelper.perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                    / height.toFloat(), 1f, 10f
        )
        setIdentityM(viewMatrix, 0)
        translateM(viewMatrix, 0, 0f, -1.5f, -5f)
        multiplyMM(
            viewProjectionMatrix, 0, projectionMatrix, 0,
            viewMatrix, 0
        )
    }

    override fun onDrawFrame(glUnused: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        val currentTime = (System.nanoTime() - globalStartTime) / 1000000000f

        redParticleShooter!!.addParticles(particleSystem!!, currentTime, 5)
        greenParticleShooter!!.addParticles(particleSystem!!, currentTime, 5)
        blueParticleShooter!!.addParticles(particleSystem!!, currentTime, 5)

        particleProgram!!.useProgram();
        particleProgram!!.setUniforms(viewProjectionMatrix, currentTime, texture);
        particleSystem!!.bindData(particleProgram!!);
        particleSystem!!.draw();
    }
}