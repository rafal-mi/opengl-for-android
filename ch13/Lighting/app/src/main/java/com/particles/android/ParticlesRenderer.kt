package com.particles.android

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_LEQUAL
import android.opengl.GLES20.GL_LESS
import android.opengl.GLES20.GL_ONE
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDepthFunc
import android.opengl.GLES20.glDepthMask
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix.invertM
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.multiplyMV
import android.opengl.Matrix.rotateM
import android.opengl.Matrix.scaleM
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import android.opengl.Matrix.transposeM
import com.particles.android.objects.Heightmap
import com.particles.android.objects.ParticleShooter
import com.particles.android.objects.ParticleSystem
import com.particles.android.objects.Skybox
import com.particles.android.programs.HeightmapShaderProgram
import com.particles.android.programs.ParticleShaderProgram
import com.particles.android.programs.SkyboxShaderProgram
import com.particles.android.util.Geometry
import com.particles.android.util.Geometry.Point
import com.particles.android.util.MatrixHelper
import com.particles.android.util.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ParticlesRenderer(private val context: Context) : Renderer {
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewMatrixForSkybox = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)
    private val it_modelViewMatrix = FloatArray(16)


    private val tempMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

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

    private var heightmapProgram: HeightmapShaderProgram? = null
    private var heightmap: Heightmap? = null

    // private val vectorToLight = Vector(0.30f, 0.35f, -0.89f).normalize()

    val vectorToLight = floatArrayOf(0.30f, 0.35f, -0.89f, 0f)

    private val pointLightPositions = floatArrayOf(
        -1f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f
    )

    private val pointLightColors = floatArrayOf(
        1.00f, 0.20f, 0.02f,
        0.02f, 0.25f, 0.02f,
        0.02f, 0.20f, 1.00f
    )

    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        heightmapProgram = HeightmapShaderProgram(context)
        heightmap = Heightmap(
            (context.resources
                .getDrawable(R.drawable.heightmap) as BitmapDrawable).bitmap
        )

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

        skyboxTexture = TextureHelper.loadCubeMap(
            context, intArrayOf(
                R.drawable.night_left, R.drawable.night_right,
                R.drawable.night_bottom, R.drawable.night_top,
                R.drawable.night_front, R.drawable.night_back
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
        updateViewMatrices();
    }

    private fun updateViewMatrices() {
        setIdentityM(viewMatrix, 0)
        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f)
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f)
        System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.size)

        // We want the translation to apply to the regular view matrix, and not
        // the skybox.
        translateM(viewMatrix, 0, 0f, -1.5f, -5f)
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        MatrixHelper.perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                    / height.toFloat(), 1f, 100f
        )
        updateViewMatrices()
    }

    override fun onDrawFrame(glUnused: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        drawSkybox();
        drawHeightmap()
        drawParticles();
    }

    private fun drawHeightmap() {
        setIdentityM(modelMatrix, 0)
        scaleM(modelMatrix, 0, 100f, 10f, 100f)
        updateMvpMatrix()
        heightmapProgram!!.useProgram()

        val vectorToLightInEyeSpace = FloatArray(4)
        val pointPositionsInEyeSpace = FloatArray(12)
        multiplyMV(vectorToLightInEyeSpace, 0, viewMatrix, 0, vectorToLight, 0)
        multiplyMV(pointPositionsInEyeSpace, 0, viewMatrix, 0, pointLightPositions, 0)
        multiplyMV(pointPositionsInEyeSpace, 4, viewMatrix, 0, pointLightPositions, 4)
        multiplyMV(pointPositionsInEyeSpace, 8, viewMatrix, 0, pointLightPositions, 8)

        heightmapProgram!!.setUniforms(
            modelViewMatrix, it_modelViewMatrix,
            modelViewProjectionMatrix, vectorToLightInEyeSpace,
            pointPositionsInEyeSpace, pointLightColors
        )

        heightmap!!.bindData(heightmapProgram!!)
        heightmap!!.draw()
    }

    private fun drawSkybox() {
        setIdentityM(modelMatrix, 0);
        updateMvpMatrixForSkybox();

        glDepthFunc(GL_LEQUAL);
        skyboxProgram!!.useProgram();
        skyboxProgram!!.setUniforms(modelViewProjectionMatrix, skyboxTexture);
        skybox!!.bindData(skyboxProgram!!)
        skybox!!.draw();
        glDepthFunc(GL_LESS);
    }

    private fun drawParticles() {
        val currentTime = (System.nanoTime() - globalStartTime) / 1000000000f
        redParticleShooter!!.addParticles(particleSystem!!, currentTime, 1)
        greenParticleShooter!!.addParticles(particleSystem!!, currentTime, 1)
        blueParticleShooter!!.addParticles(particleSystem!!, currentTime, 1)

        setIdentityM(modelMatrix, 0);
        updateMvpMatrix();

        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE)

        glDepthMask(false);
        particleProgram!!.useProgram()
        particleProgram!!.setUniforms(modelViewProjectionMatrix, currentTime, particleTexture)
        particleSystem!!.bindData(particleProgram!!)
        particleSystem!!.draw()
        glDepthMask(true);

        glDisable(GL_BLEND)
    }

    private fun updateMvpMatrix() {
        multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        invertM(tempMatrix, 0, modelViewMatrix, 0)
        transposeM(it_modelViewMatrix, 0, tempMatrix, 0)
        multiplyMM(
            modelViewProjectionMatrix, 0,
            projectionMatrix, 0,
            modelViewMatrix, 0
        )
    }

    private fun updateMvpMatrixForSkybox() {
        multiplyMM(tempMatrix, 0, viewMatrixForSkybox, 0, modelMatrix, 0)
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
    }

}