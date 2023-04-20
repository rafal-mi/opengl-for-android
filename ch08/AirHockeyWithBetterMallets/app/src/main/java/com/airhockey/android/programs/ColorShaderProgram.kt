package com.airhockey.android.programs

import android.content.Context
import android.opengl.GLES20.*
import com.airhockey.android.R

class ColorShaderProgram(context: Context): ShaderProgram(context, R.raw.simple_vertex_shader, R.raw.simple_fragment_shader) {
    // Uniform locations
    private var uMatrixLocation = 0
    var uColorLocation = 0

    // Attribute locations
    var aPositionLocation = 0

    init {
        // Retrieve uniform locations for the shader program.
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        // Retrieve attribute locations for the shader program.
        aPositionLocation = glGetAttribLocation(program, A_POSITION);

        uColorLocation = glGetUniformLocation(program, U_COLOR)
    }

    fun setUniforms(matrix: FloatArray?, r: Float, g: Float, b: Float) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        glUniform4f(uColorLocation, r, g, b, 1f);
    }
}