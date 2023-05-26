package com.particles.android.programs

import android.content.Context
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform3fv
import android.opengl.GLES20.glUniform4fv
import android.opengl.GLES20.glUniformMatrix4fv
import com.particles.android.R

class HeightmapShaderProgram(context: Context)
    : ShaderProgram(context, R.raw.heightmap_vertex_shader, R.raw.heightmap_fragment_shader)
{
    var uVectorToLightLocation = 0
    var uMVMatrixLocation = 0
    var uIT_MVMatrixLocation = 0
    var uMVPMatrixLocation = 0
    var uPointLightPositionsLocation = 0
    var uPointLightColorsLocation = 0

    var aPositionLocation = 0
    var aNormalLocation = 0

    init {
        uVectorToLightLocation = glGetUniformLocation(program, U_VECTOR_TO_LIGHT);
        uMVMatrixLocation = glGetUniformLocation(program, U_MV_MATRIX);
        uIT_MVMatrixLocation = glGetUniformLocation(program, U_IT_MV_MATRIX);
        uMVPMatrixLocation = glGetUniformLocation(program, U_MVP_MATRIX);

        uPointLightPositionsLocation =
            glGetUniformLocation(program, U_POINT_LIGHT_POSITIONS);
        uPointLightColorsLocation =
            glGetUniformLocation(program, U_POINT_LIGHT_COLORS);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aNormalLocation = glGetAttribLocation(program, A_NORMAL);

    }

    fun setUniforms(
        mvMatrix: FloatArray?,
//        it_mvMatrix: FloatArray?,
//        mvpMatrix: FloatArray?,
//        vectorToDirectionalLight: FloatArray?,
//        pointLightPositions: FloatArray?,
//        pointLightColors: FloatArray?
    ) {
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0)
//        glUniformMatrix4fv(uIT_MVMatrixLocation, 1, false, it_mvMatrix, 0)
//        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0)
//        glUniform3fv(uVectorToLightLocation, 1, vectorToDirectionalLight, 0)
//        glUniform4fv(uPointLightPositionsLocation, 3, pointLightPositions, 0)
//        glUniform3fv(uPointLightColorsLocation, 3, pointLightColors, 0)
    }

}