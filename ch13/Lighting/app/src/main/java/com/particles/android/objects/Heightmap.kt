package com.particles.android.objects

import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_UNSIGNED_SHORT
import android.opengl.GLES20.glBindBuffer
import android.opengl.GLES20.glDrawElements
import com.particles.android.Constants.Companion.BYTES_PER_FLOAT
import com.particles.android.data.IndexBuffer
import com.particles.android.data.VertexBuffer
import com.particles.android.programs.HeightmapShaderProgram
import com.particles.android.util.Geometry
import com.particles.android.util.Geometry.*


class Heightmap(bitmap: Bitmap) {
    var width = 0
    var height = 0
    var numElements = 0
    var vertexBuffer: VertexBuffer? = null
    var indexBuffer: IndexBuffer? = null

    var aPositionLocation = 0
    var aNormalLocation = 0

    init {
        width = bitmap.width;
        height = bitmap.height;
        if (width * height > 65536) {
            throw RuntimeException("Heightmap is too large for the index buffer.");
        }
        numElements = calculateNumElements()
        vertexBuffer = VertexBuffer(loadBitmapData(bitmap))
        indexBuffer = IndexBuffer(createIndexData())
    }

    /**
     * Copy the heightmap data into a vertex buffer object.
     */
    private fun loadBitmapData(bitmap: Bitmap): FloatArray {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.recycle()
        val heightmapVertices = FloatArray(width * height * TOTAL_COMPONENT_COUNT)
        var offset = 0
        for (row in 0 until height) {
            for (col in 0 until width) {
                // The heightmap will lie flat on the XZ plane and centered
                // around (0, 0), with the bitmap width mapped to X and the
                // bitmap height mapped to Z, and Y representing the height. We
                // assume the heightmap is grayscale, and use the value of the
                // red color to determine the height.
                val point: Point = getPoint(pixels, row, col)
                heightmapVertices[offset++] = point.x
                heightmapVertices[offset++] = point.y
                heightmapVertices[offset++] = point.z

                val top: Point = getPoint(pixels, row - 1, col)
                val left: Point = getPoint(pixels, row, col - 1)
                val right: Point = getPoint(pixels, row, col + 1)
                val bottom: Point = getPoint(pixels, row + 1, col)

                val rightToLeft: Vector = Geometry.vectorBetween(right, left)
                val topToBottom: Vector = Geometry.vectorBetween(top, bottom)
                val normal: Vector = rightToLeft.crossProduct(topToBottom).normalize()

                heightmapVertices[offset++] = normal.x
                heightmapVertices[offset++] = normal.y
                heightmapVertices[offset++] = normal.z
            }
        }
        return heightmapVertices
    }

    private fun getPoint(pixels: IntArray, row: Int, col: Int): Point {
        var row = row
        var col = col
        val x = col.toFloat() / (width - 1).toFloat() - 0.5f
        val z = row.toFloat() / (height - 1).toFloat() - 0.5f
        row = clamp(row, 0, width - 1)
        col = clamp(col, 0, height - 1)
        val y = Color.red(pixels[row * height + col]).toFloat() / 255f
        return Point(x, y, z)
    }

    private fun clamp(value: Int, min: Int, max: Int): Int {
        return min.coerceAtLeast(max.coerceAtMost(value))
    }


    private fun calculateNumElements(): Int {
        return (width - 1) * (height - 1) * 2 * 3
    }

    private fun createIndexData(): ShortArray {
        val indexData = ShortArray(numElements)
        var offset = 0
        for (row in 0 until height - 1) {
            for (col in 0 until width - 1) {
                val topLeftIndexNum = (row * width + col).toShort()
                val topRightIndexNum = (row * width + col + 1).toShort()
                val bottomLeftIndexNum = ((row + 1) * width + col).toShort()
                val bottomRightIndexNum = ((row + 1) * width + col + 1).toShort()
                // Write out two triangles.
                indexData[offset++] = topLeftIndexNum
                indexData[offset++] = bottomLeftIndexNum
                indexData[offset++] = topRightIndexNum
                indexData[offset++] = topRightIndexNum
                indexData[offset++] = bottomLeftIndexNum
                indexData[offset++] = bottomRightIndexNum
            }
        }
        return indexData
    }

    fun bindData(heightmapProgram: HeightmapShaderProgram) {
        vertexBuffer!!.setVertexAttribPointer(
            0,
            heightmapProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT, STRIDE
        )
        vertexBuffer!!.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT,
            heightmapProgram.aNormalLocation,
            NORMAL_COMPONENT_COUNT, STRIDE
        )
    }

    fun draw() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer!!.bufferId)
        glDrawElements(GL_TRIANGLES, numElements, GL_UNSIGNED_SHORT, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    companion object {
        const val POSITION_COMPONENT_COUNT = 3
        const val NORMAL_COMPONENT_COUNT = 3
        const val TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT
        const val STRIDE =
            (POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }

}