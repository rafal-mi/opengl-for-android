package com.airhockey.android.objects

import android.opengl.GLES20.*
import android.opengl.GLES20.glDrawArrays
import com.airhockey.android.AirHockeyRenderer.Companion.POSITION_COMPONENT_COUNT
import com.airhockey.android.AirHockeyRenderer.Companion.STRIDE
import com.airhockey.android.Constants.Companion.BYTES_PER_FLOAT
import com.airhockey.android.data.VertexArray
import com.airhockey.android.programs.TextureShaderProgram

class Table {
    val vertexArray = VertexArray(VERTEX_DATA)

    fun bindData(textureProgram: TextureShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            textureProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT,
            STRIDE);
        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            textureProgram.aTextureCoordinatesLocation,
            TEXTURE_COORDINATES_COMPONENT_COUNT,
            STRIDE);
    }

    fun draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }

    companion object {
        val POSITION_COMPONENT_COUNT = 2
        val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        val STRIDE = (POSITION_COMPONENT_COUNT
                + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT

        val VERTEX_DATA = floatArrayOf(
            // Order of coordinates: X, Y, S, T

            // Triangle Fan
            0f,    0f, 0.5f, 0.5f,
            -0.5f, -0.8f,   0f, 0.9f,
            0.5f, -0.8f,   1f, 0.9f,
            0.5f,  0.8f,   1f, 0.1f,
            -0.5f,  0.8f,   0f, 0.1f,
            -0.5f, -0.8f,   0f, 0.9f
        )

    }
}