package com.particles.android.programs

import android.content.Context
import android.opengl.GLES20.glUseProgram
import com.particles.android.util.ShaderHelper
import com.particles.android.util.TextResourceReader


abstract class ShaderProgram(
    context: Context,
    vertexShaderResourceId: Int,
    fragmentShaderResourceId: Int
) {
    // Uniform constants
    protected val U_MATRIX = "u_Matrix"
    protected val U_TEXTURE_UNIT = "u_TextureUnit"
    protected val U_TIME = "u_Time"

    // Attribute constants
    protected val A_POSITION = "a_Position"
    protected val A_COLOR = "a_Color"
    protected val A_TEXTURE_COORDINATES = "a_TextureCoordinates"

    protected val A_DIRECTION_VECTOR = "a_DirectionVector"
    protected val A_PARTICLE_START_TIME = "a_ParticleStartTime"

    // Shader program
    var program = ShaderHelper.buildProgram(
        TextResourceReader.readTextFileFromResource(
            context, vertexShaderResourceId
        ),
        TextResourceReader.readTextFileFromResource(
            context, fragmentShaderResourceId
        )
    )

    fun useProgram() {
        // Set the current OpenGL shader program to this program.
        glUseProgram(program)
    }

    companion object {
        const val  U_COLOR = "u_Color";
        const val U_VECTOR_TO_LIGHT = "u_VectorToLight"
        const val A_NORMAL = "a_Normal"
        const val U_MV_MATRIX = "u_MVMatrix"
        const val U_IT_MV_MATRIX = "u_IT_MVMatrix"
        const val U_MVP_MATRIX = "u_MVPMatrix"
        const val U_POINT_LIGHT_POSITIONS = "u_PointLightPositions"
        const val U_POINT_LIGHT_COLORS = "u_PointLightColors"
    }
}