package com.particles.android.programs

import android.content.Context
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform3f
import android.opengl.GLES20.glUniformMatrix4fv
import com.particles.android.R
import com.particles.android.util.Geometry

class HeightmapShaderProgram(context: Context)
    : ShaderProgram(context, R.raw.heightmap_vertex_shader, R.raw.heightmap_fragment_shader)
{
    var uMatrixLocation = 0
    var aPositionLocation = 0

    var uVectorToLightLocation = 0
    var aNormalLocation = 0

    init {
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);

        uVectorToLightLocation = glGetUniformLocation(program, U_VECTOR_TO_LIGHT);
        aNormalLocation = glGetAttribLocation(program, A_NORMAL);
    }

    fun setUniforms(matrix: FloatArray?, vectorToLight: Geometry.Vector) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glUniform3f(uVectorToLightLocation,
            vectorToLight.x, vectorToLight.y, vectorToLight.z)
    }
}