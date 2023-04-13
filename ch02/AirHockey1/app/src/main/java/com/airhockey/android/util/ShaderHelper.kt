package com.airhockey.android.util

import android.opengl.GLES20.*
import android.util.Log
import com.airhockey.android.App.Companion.TAG

class ShaderHelper {
    companion object {
        fun compileVertexShader(shaderCode: String) =
            compileShader(GL_VERTEX_SHADER, shaderCode)

        fun compileFragmentShader(shaderCode: String) =
            compileShader(GL_FRAGMENT_SHADER, shaderCode)

        private fun compileShader(type: Int, shaderCode: String): Int {
            val shaderObjectId = glCreateShader(type)

            if (shaderObjectId == 0) {
                if (LoggerConfig.ON) {
                    Log.w(TAG, "Could not create new shader.");
                }
                return 0
            }

            glShaderSource(shaderObjectId, shaderCode)
            glCompileShader(shaderObjectId)

            val compileStatus = IntArray(1)
            glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)

            if (LoggerConfig.ON) {
                // Print the shader info log to the Android log output.
                Log.v(TAG, "Results of compiling source:" + "\n" + shaderCode + "\n:"
                        + glGetShaderInfoLog(shaderObjectId));
            }

            return shaderObjectId
        }

    }
}