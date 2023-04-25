package com.airhockey.android.programs

import android.content.Context
import android.opengl.GLES20.*
import com.airhockey.android.R


class TextureShaderProgram(
    context: Context
): ShaderProgram(context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader) {
    // Uniform locations
    private var uMatrixLocation = 0
    private var uTextureUnitLocation = 0

    // Attribute locations
    var aPositionLocation = 0
    var aTextureCoordinatesLocation = 0

    init {
        // Retrieve uniform locations for the shader program.
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);

        // Retrieve attribute locations for the shader program.
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation =
            glGetAttribLocation(program, A_TEXTURE_COORDINATES);
    }

    fun setUniforms(matrix: FloatArray, textureId: Int) {
        // Pass the matrix into the shader program.
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)

        // Set the active texture unit to texture unit 0.
        glActiveTexture(GL_TEXTURE0)

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId)

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        glUniform1i(uTextureUnitLocation, 0)
    }




}