package com.particles.android

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_ONE
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.rotateM
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import com.particles.android.objects.ParticleShooter
import com.particles.android.objects.ParticleSystem
import com.particles.android.objects.Skybox
import com.particles.android.programs.ParticleShaderProgram
import com.particles.android.programs.SkyboxShaderProgram
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

    private var skyboxProgram: SkyboxShaderProgram? = null
    private var skybox: Skybox? = null

    private var particleProgram: ParticleShaderProgram? = null
    var particleSystem: ParticleSystem? = null
    var redParticleShooter: ParticleShooter? = null
    var greenParticleShooter: ParticleShooter? = null
    var blueParticleShooter: ParticleShooter? = null

    var globalStartTime: Long = 0
    private var particleTexture: Int = 0
    private var skyboxTexture = 0

    val angleVarianceInDegrees = 5f
    val speedVariance = 1f

    var xRotation = 0f
    var yRotation = 0f

    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        skyboxProgram = SkyboxShaderProgram(context)
        skybox = Skybox()

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

        particleTexture = TextureHelper.loadTexture(context, R.drawable.particle_texture)

        skyboxTexture = TextureHelper.loadCubeMap(
            context, intArrayOf(
                R.drawable.left, R.drawable.right,
                R.drawable.bottom, R.drawable.top,
                R.drawable.front, R.drawable.back
            )
        )
    }

    fun handleTouchDrag(deltaX: Float, deltaY: Float) {
        xRotation += deltaX / 1024f
        yRotation += deltaY / 1024f
        if (yRotation < -90) {
            yRotation = -90f
        } else if (yRotation > 90) {
            yRotation = 90f
        }
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        MatrixHelper.perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                    / height.toFloat(), 1f, 10f
        )
    }

    override fun onDrawFrame(glUnused: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        drawSkybox();
        drawParticles();
    }

    private fun drawSkybox() {
        setIdentityM(viewMatrix, 0);
        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f);
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f);
        // translateM(viewMatrix, 0, 0f, -1.5f, -5f);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        skyboxProgram!!.useProgram();
        skyboxProgram!!.setUniforms(viewProjectionMatrix, skyboxTexture);
        skybox!!.bindData(skyboxProgram!!)
        skybox!!.draw();
    }

    private fun drawParticles() {
        val currentTime = (System.nanoTime() - globalStartTime) / 1000000000f
        redParticleShooter!!.addParticles(particleSystem!!, currentTime, 1)
        greenParticleShooter!!.addParticles(particleSystem!!, currentTime, 1)
        blueParticleShooter!!.addParticles(particleSystem!!, currentTime, 1)

        setIdentityM(viewMatrix, 0)
        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f)
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f)
        translateM(viewMatrix, 0, 0f, -1.5f, -5f)
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE)

        particleProgram!!.useProgram()
        particleProgram!!.setUniforms(viewProjectionMatrix, currentTime, particleTexture)
        particleSystem!!.bindData(particleProgram!!)
        particleSystem!!.draw()

        glDisable(GL_BLEND)
    }


}