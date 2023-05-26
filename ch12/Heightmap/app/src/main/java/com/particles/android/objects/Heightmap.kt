package com.particles.android.objects

import android.graphics.Bitmap
import android.graphics.Color
import com.particles.android.data.IndexBuffer
import com.particles.android.data.VertexBuffer


class Heightmap(bitmap: Bitmap) {
    var width = 0
    var height = 0
    var numElements = 0
    var vertexBuffer: VertexBuffer? = null
    var indexBuffer: IndexBuffer? = null

    init {
        width = bitmap.width;
        height = bitmap.height;
        if (width * height > 65536) {
            throw RuntimeException("Heightmap is too large for the index buffer.");
        }
        numElements = calculateNumElements();
        vertexBuffer = VertexBuffer(loadBitmapData(bitmap));
        indexBuffer = IndexBuffer(createIndexData());
    }

    /**
     * Copy the heightmap data into a vertex buffer object.
     */
    private fun loadBitmapData(bitmap: Bitmap): FloatArray {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.recycle()
        val heightmapVertices = FloatArray(width * height * POSITION_COMPONENT_COUNT)
        var offset = 0
        for (row in 0 until height) {
            for (col in 0 until width) {
                // The heightmap will lie flat on the XZ plane and centered
                // around (0, 0), with the bitmap width mapped to X and the
                // bitmap height mapped to Z, and Y representing the height. We
                // assume the heightmap is grayscale, and use the value of the
                // red color to determine the height.
                val xPosition = col.toFloat() / (width - 1).toFloat() - 0.5f
                val yPosition = Color.red(pixels[row * height + col]) as Float / 255f
                val zPosition = row.toFloat() / (height - 1).toFloat() - 0.5f
                heightmapVertices[offset++] = xPosition
                heightmapVertices[offset++] = yPosition
                heightmapVertices[offset++] = zPosition
            }
        }
        return heightmapVertices
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

    companion object {
        const val POSITION_COMPONENT_COUNT = 3
    }

}