package com.particles.android.programs

import android.content.Context
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniformMatrix4fv
import com.particles.android.R

class HeightmapShaderProgram(context: Context)
    : ShaderProgram(context, R.raw.heightmap_vertex_shader, R.raw.heightmap_fragment_shader)
{
    var uMatrixLocation = 0
    var aPositionLocation = 0

    init {
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
    }

    fun setUniforms(matrix: FloatArray?) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }
}