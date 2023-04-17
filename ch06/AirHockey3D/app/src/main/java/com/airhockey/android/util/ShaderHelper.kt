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
                    Log.w(TAG, "Could not create new shader.")
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
                        + glGetShaderInfoLog(shaderObjectId))
            }

            if (compileStatus[0] == 0) {
                // If it failed, delete the shader object.
                glDeleteShader(shaderObjectId);

                if (LoggerConfig.ON) {
                    Log.w(TAG, "Compilation of shader failed.");
                }

                return 0;
            }

            return shaderObjectId
        }

        fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
            val programObjectId = glCreateProgram()

            if (programObjectId == 0) {
                if (LoggerConfig.ON) {
                    Log.w(TAG, "Could not create new program")
                }
                return 0
            }

            glAttachShader(programObjectId, vertexShaderId)
            glAttachShader(programObjectId, fragmentShaderId)

            glLinkProgram(programObjectId)

            val linkStatus = IntArray(1)
            glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0)

            if (LoggerConfig.ON) {
                // Print the program info log to the Android log output.
                Log.v(TAG, "Results of linking program:\n"
                        + glGetProgramInfoLog(programObjectId));
            }

            if (linkStatus[0] == 0) {
                // If it failed, delete the program object.
                glDeleteProgram(programObjectId);
                if (LoggerConfig.ON) {
                    Log.w(TAG, "Linking of program failed.")
                }
                return 0
            }

            return programObjectId
        }

        fun validateProgram(programObjectId: Int): Boolean {
            glValidateProgram(programObjectId)
            val validateStatus = IntArray(1)
            glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0)

            Log.v(TAG, "Results of validating program: " + validateStatus[0]
                    + "\nLog:" + glGetProgramInfoLog(programObjectId))

            return validateStatus[0] != 0
        }

    }
}