package com.airhockey.android.objects

import android.opengl.GLES20.GL_POINTS
import android.opengl.GLES20.glDrawArrays
import com.airhockey.android.Constants.Companion.BYTES_PER_FLOAT
import com.airhockey.android.data.VertexArray
import com.airhockey.android.programs.ColorShaderProgram


class Mallet {
    private val vertexArray = VertexArray(VERTEX_DATA)

    fun bindData(colorProgram: ColorShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            colorProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT,
            STRIDE);

        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            colorProgram.aColorLocation,
            COLOR_COMPONENT_COUNT,
            STRIDE);
    }

    fun draw() {
        glDrawArrays(GL_POINTS, 0, 2);
    }

    companion object {
        const val POSITION_COMPONENT_COUNT = 2
        const val COLOR_COMPONENT_COUNT = 3
        const val STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT

        val VERTEX_DATA = floatArrayOf(
            // Order of coordinates: X, Y, R, G, B
            0f, -0.4f, 0f, 0f, 1f,
            0f, 0.4f, 1f, 0f, 0f
        )
    }
}