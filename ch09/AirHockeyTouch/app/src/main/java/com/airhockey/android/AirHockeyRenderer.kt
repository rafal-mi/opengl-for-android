package com.airhockey.android

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix.*
import android.util.Log
import com.airhockey.android.App.Companion.TAG
import com.airhockey.android.Constants.Companion.BYTES_PER_FLOAT
import com.airhockey.android.objects.Mallet
import com.airhockey.android.objects.Puck
import com.airhockey.android.objects.Table
import com.airhockey.android.programs.ColorShaderProgram
import com.airhockey.android.programs.TextureShaderProgram
import com.airhockey.android.util.Geometry
import com.airhockey.android.util.Geometry.*
import com.airhockey.android.util.MatrixHelper
import com.airhockey.android.util.TextureHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class AirHockeyRenderer(private val context: Context) : Renderer {
    var vertexData: FloatBuffer? = null
    private var projectionMatrix = FloatArray(16)
    private var modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)
    private val invertedViewProjectionMatrix = FloatArray(16)


    private lateinit var table: Table
    private lateinit var mallet: Mallet
    private lateinit var puck: Puck

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram

    private var texture = 0

    private var program = -1
    // private var uColorLocation = -1
    private var aPositionLocation = -1
    private var aColorLocation = -1
    private var uMatrixLocation = -1

    private var malletPressed = false
    private lateinit var blueMalletPosition: Point

    private val leftBound = -0.5f
    private val rightBound = 0.5f
    private val farBound = -0.8f
    private val nearBound = 0.8f

    private var previousBlueMalletPosition: Point? = null

    private var puckPosition: Point? = null
    private var puckVector: Vector? = null

    init {
        val tableVerticesWithTriangles = floatArrayOf(
            // Order of coordinates: X, Y, R, G, B

            // Triangle Fan
            0f,    0f,   1f,   1f,   1f,
            -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
            0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
            0.5f,  0.8f, 0.7f, 0.7f, 0.7f,
            -0.5f,  0.8f, 0.7f, 0.7f, 0.7f,
            -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,

            // Line 1
            -0.5f, 0f, 1f, 0f, 0f,
            0.5f, 0f, 1f, 0f, 0f,

            // Mallets
            0f, -0.25f, 0f, 0f, 1f,
            0f,  0.25f, 1f, 0f, 0f
        )

        vertexData = ByteBuffer
            .allocateDirect(tableVerticesWithTriangles.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        (vertexData as FloatBuffer).put(tableVerticesWithTriangles)
    }

    fun handleTouchPress(normalizedX: Float, normalizedY: Float) {
        val ray: Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)

        // Now test if this ray intersects with the mallet by creating a
        // bounding sphere that wraps the mallet.
        val malletBoundingSphere = Sphere(
            Point(
                blueMalletPosition.x,
                blueMalletPosition.y,
                blueMalletPosition.z
            ),
            mallet.height / 2f
        )

        // If the ray intersects (if the user touched a part of the screen that
        // intersects the mallet's bounding sphere), then set malletPressed =
        // true.
        malletPressed = Geometry.intersects(malletBoundingSphere, ray)

        Log.d(TAG, "Mallet pressed? $malletPressed")
    }

    private fun convertNormalized2DPointToRay(
        normalizedX: Float, normalizedY: Float
    ): Ray {
        // We'll convert these normalized device coordinates into world-space
        // coordinates. We'll pick a point on the near and far planes, and draw a
        // line between them. To do this transform, we need to first multiply by
        // the inverse matrix, and then we need to undo the perspective divide.
        val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1f, 1f)
        val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)
        val nearPointWorld = FloatArray(4)
        val farPointWorld = FloatArray(4)
        multiplyMV(
            nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0
        )
        multiplyMV(
            farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0
        )

        // Why are we dividing by W? We multiplied our vector by an inverse
        // matrix, so the W value that we end up is actually the *inverse* of
        // what the projection matrix would create. By dividing all 3 components
        // by W, we effectively undo the hardware perspective divide.
        divideByW(nearPointWorld)
        divideByW(farPointWorld)

        // We don't care about the W value anymore, because our points are now
        // in world coordinates.
        val nearPointRay = Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])
        val farPointRay = Point(farPointWorld[0], farPointWorld[1], farPointWorld[2])
        return Ray(
            nearPointRay,
            Geometry.vectorBetween(nearPointRay, farPointRay)
        )
    }

    private fun divideByW(vector: FloatArray) {
        vector[0] /= vector[3]
        vector[1] /= vector[3]
        vector[2] /= vector[3]
    }

    fun handleTouchDrag(normalizedX: Float, normalizedY: Float) {
        if (malletPressed) {
            val ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
            // Define a plane representing our air hockey table.
            val plane = Geometry.Plane(Geometry.Point(0f, 0f, 0f), Geometry.Vector(0f, 1f, 0f))
            // Find out where the touched point intersects the plane
            // representing our table. We'll move the mallet along this plane.
            val touchedPoint = Geometry.intersectionPoint(ray, plane)
            // Clamp to bounds
            previousBlueMalletPosition = blueMalletPosition
            /*
            blueMalletPosition =
                new Point(touchedPoint.x, mallet.height / 2f, touchedPoint.z);
            */
            // Clamp to bounds
            blueMalletPosition = Point(
                clamp(
                    touchedPoint!!.x,
                    leftBound + mallet.radius,
                    rightBound - mallet.radius
                ),
                mallet.height / 2f,
                clamp(
                    touchedPoint!!.z,
                    0f + mallet.radius,
                    nearBound - mallet.radius
                )
            )

            // Now test if mallet has struck the puck.
            val distance: Float = Geometry.vectorBetween(blueMalletPosition, puckPosition!!).length
            if (distance < puck.radius + mallet.radius) {
                // The mallet has struck the puck. Now send the puck flying
                // based on the mallet velocity.
                puckVector = Geometry.vectorBetween(
                    previousBlueMalletPosition!!, blueMalletPosition
                )
            }
        }
    }

    private fun clamp(value: Float, min: Float, max: Float): Float {
        return Math.min(max, Math.max(value, min))
    }


    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        table = Table()
        mallet = Mallet(0.08f, 0.15f, 32)
        puck = Puck(0.06f, 0.02f, 32)

        blueMalletPosition = Point(0f, mallet.height / 2f, 0.4f)
        puckPosition = Point(0f, puck.height / 2f, 0f)
        puckVector = Vector(0f, 0f, 0f)

        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)

        texture = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface)
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        MatrixHelper.perspectiveM(projectionMatrix, 45.toFloat(), width.toFloat() / height.toFloat(), 1f, 10f)
        setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.9f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(glUnused: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        // Translate the puck by its vector
        puckPosition = puckPosition!!.translate(puckVector!!);

        // If the puck struck a side, reflect it off that side.
        // If the puck struck a side, reflect it off that side.
        if (puckPosition!!.x < leftBound + puck.radius
            || puckPosition!!.x > rightBound - puck.radius
        ) {
            puckVector = Vector(-puckVector!!.x, puckVector!!.y, puckVector!!.z)
            puckVector = puckVector!! * 0.9f
        }
        if (puckPosition!!.z < farBound + puck.radius
            || puckPosition!!.z > nearBound - puck.radius
        ) {
            puckVector = Vector(puckVector!!.x, puckVector!!.y, -puckVector!!.z)
            puckVector = puckVector!! * 0.9f
        }
        // Clamp the puck position.
        // Clamp the puck position.
        puckPosition = Point(
            clamp(puckPosition!!.x, leftBound + puck.radius, rightBound - puck.radius),
            puckPosition!!.y,
            clamp(puckPosition!!.z, farBound + puck.radius, nearBound - puck.radius)
        )

        // Friction factor

        // Friction factor
        puckVector = puckVector!! * 0.99f

        // Multiply the view and projection matrices together.
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);

        // Draw the table.
        positionTableInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, texture);
        table.bindData(textureProgram);
        table.draw();

        // Draw the mallets.
        positionObjectInScene(0f, mallet.height / 2f, -0.4f);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f);
        mallet.bindData(colorProgram);
        mallet.draw();

        positionObjectInScene(blueMalletPosition.x, blueMalletPosition.y,
            blueMalletPosition.z);
        colorProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f);
        // Note that we don't have to define the object data twice -- we just
        // draw the same mallet again but in a different position and with a
        // different color.
        mallet.draw();

        // Draw the puck.
        positionObjectInScene(puckPosition!!.x, puckPosition!!.y, puckPosition!!.z);
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 1f);
        puck.bindData(colorProgram);
        puck.draw();
    }

    private fun positionTableInScene() {
        // The table is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        setIdentityM(modelMatrix, 0)
        rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f)
        multiplyMM(
            modelViewProjectionMatrix, 0, viewProjectionMatrix,
            0, modelMatrix, 0
        )
    }

    // The mallets and the puck are positioned on the same plane as the table.
    private fun positionObjectInScene(x: Float, y: Float, z: Float) {
        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, x, y, z)
        multiplyMM(
            modelViewProjectionMatrix, 0, viewProjectionMatrix,
            0, modelMatrix, 0
        )
    }

    companion object {
        const val A_POSITION = "a_Position"
        const val A_COLOR = "a_Color"
        const val U_MATRIX = "u_Matrix"
        const val POSITION_COMPONENT_COUNT = 2
        const val COLOR_COMPONENT_COUNT = 3
        const val STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }
}