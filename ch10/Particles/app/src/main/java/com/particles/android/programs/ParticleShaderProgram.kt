package com.particles.android.programs

import android.content.Context
import android.opengl.GLES20.*
import com.particles.android.R


class ParticleShaderProgram(context: Context) : ShaderProgram(context, R.raw.particle_vertex_shader, R.raw.particle_fragment_shader) {

    // Uniform locations
    private var uMatrixLocation = 0
    private var uTimeLocation = 0

    // Attribute locations
    var aPositionLocation = 0
    var aColorLocation = 0
    var aDirectionVectorLocation = 0
    var aParticleStartTimeLocation = 0
    private var uTextureUnitLocation = 0

    init {
        // Retrieve uniform locations for the shader program.
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uTimeLocation = glGetUniformLocation(program, U_TIME);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);

        // Retrieve attribute locations for the shader program.
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aColorLocation = glGetAttribLocation(program, A_COLOR);
        aDirectionVectorLocation = glGetAttribLocation(program, A_DIRECTION_VECTOR);
        aParticleStartTimeLocation =
            glGetAttribLocation(program, A_PARTICLE_START_TIME);
    }

    fun setUniforms(matrix: FloatArray?, elapsedTime: Float, textureId: Int) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glUniform1f(uTimeLocation, elapsedTime)
    }


}