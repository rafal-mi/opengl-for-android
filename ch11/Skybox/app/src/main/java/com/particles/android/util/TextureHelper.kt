package com.particles.android.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20.GL_LINEAR
import android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_CUBE_MAP
import android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X
import android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y
import android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
import android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X
import android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y
import android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glDeleteTextures
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glGenerateMipmap
import android.opengl.GLES20.glTexParameteri
import android.opengl.GLUtils.texImage2D
import android.util.Log
import com.particles.android.App.Companion.TAG


class TextureHelper {
    companion object {
        fun loadTexture(context: Context, resourceId: Int): Int {
            val textureObjectIds = IntArray(1)
            glGenTextures(1, textureObjectIds, 0)

            if (textureObjectIds[0] == 0) {
                if (LoggerConfig.ON) {
                    Log.w(TAG, "Could not generate a new OpenGL texture object.")
                }
                return 0
            }

            val options = BitmapFactory.Options()
            options.inScaled = false
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

            if (bitmap == null) {
                if (LoggerConfig.ON) {
                    Log.w(TAG, "Resource ID " + resourceId + " could not be decoded.");
                }
                glDeleteTextures(1, textureObjectIds, 0);
                return 0;
            }

            glBindTexture(GL_TEXTURE_2D, textureObjectIds[0])

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
            glGenerateMipmap(GL_TEXTURE_2D)

            glBindTexture(GL_TEXTURE_2D, 0)

            return textureObjectIds[0]
        }

        fun loadCubeMap(context: Context, cubeResources: IntArray): Int {
            val textureObjectIds = IntArray(1)
            glGenTextures(1, textureObjectIds, 0)
            if (textureObjectIds[0] == 0) {
                if (LoggerConfig.ON) {
                    Log.w(TAG, "Could not generate a new OpenGL texture object.")
                }
                return 0
            }
            val options = BitmapFactory.Options()
            options.inScaled = false
            val cubeBitmaps = arrayOfNulls<Bitmap>(6)
            for (i in 0..5) {
                cubeBitmaps[i] = BitmapFactory.decodeResource(
                    context.resources,
                    cubeResources[i], options
                )
                if (cubeBitmaps[i] == null) {
                    if (LoggerConfig.ON) {
                        Log.w(
                            TAG, "Resource ID " + cubeResources[i]
                                    + " could not be decoded."
                        )
                    }
                    glDeleteTextures(1, textureObjectIds, 0)
                    return 0
                }
            }
            // Linear filtering for minification and magnification
            glBindTexture(GL_TEXTURE_CUBE_MAP, textureObjectIds[0])
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBitmaps[0], 0)
            texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBitmaps[1], 0)
            texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBitmaps[2], 0)
            texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBitmaps[3], 0)
            texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBitmaps[4], 0)
            texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBitmaps[5], 0)
            glBindTexture(GL_TEXTURE_2D, 0)
            for (bitmap in cubeBitmaps) {
                bitmap!!.recycle()
            }
            return textureObjectIds[0]
        }
    }


}