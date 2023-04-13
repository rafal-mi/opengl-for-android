package com.airhockey.android

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView.Renderer
import com.airhockey.android.util.ShaderHelper
import com.airhockey.android.util.TextResourceReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AirHockeyRenderer(private val context: Context) : Renderer {
    var vertexData: FloatBuffer? = null

    init {
        val tableVerticesWithTriangles = arrayOf(
            // Triangle 1
            0f, 0f,
            9f, 14f,
            0f, 14f,

            // Triangle 2
            0f, 0f,
            9f, 0f,
            9f, 14f,

            // Line 1
            0f, 7f,
            9f, 7f,

            // Mallets
            4.5f, 2f,
            4.5f, 12f
        )

        vertexData = ByteBuffer
            .allocateDirect(tableVerticesWithTriangles.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    }

    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f)

        val vertexShaderSource = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_vertex_shader)
        val fragmentShaderSource = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_fragment_shader)

        val vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource)
        val fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource)
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(glUnused: GL10?) {
        glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    companion object {
        val POSITION_COMPONENT_COUNT = 2

        val BYTES_PER_FLOAT = 4
    }
}