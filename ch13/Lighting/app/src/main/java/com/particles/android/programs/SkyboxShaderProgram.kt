package com.particles.android.programs

import android.content.Context
import android.opengl.GLES20.*
import com.particles.android.R


class SkyboxShaderProgram(context: Context) : ShaderProgram(context, R.raw.skybox_vertex_shader, R.raw.skybox_fragment_shader) {
    var uMatrixLocation = 0
    var uTextureUnitLocation = 0
    var aPositionLocation = 0

    init {
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
    }

    fun setUniforms(matrix: FloatArray?, textureId: Int) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId)
        glUniform1i(uTextureUnitLocation, 0)
    }
}