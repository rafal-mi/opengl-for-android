package com.airhockey.android.objects

import com.airhockey.android.data.VertexArray
import com.airhockey.android.objects.ObjectBuilder.DrawCommand
import com.airhockey.android.programs.ColorShaderProgram
import com.airhockey.android.util.Geometry


class Mallet(radius: Float, height: Float, numPointsAroundMallet: Int) {
    val radius: Float
    val height: Float
    private val vertexArray: VertexArray
    private val drawList: List<DrawCommand>

    init {
        val generatedData: ObjectBuilder.GeneratedData = ObjectBuilder.createMallet(
            Geometry.Point(
                0f,
                0f, 0f
            ), radius, height, numPointsAroundMallet
        )
        this.radius = radius
        this.height = height
        vertexArray = VertexArray(generatedData.vertexData)
        drawList = generatedData.drawList
    }

    fun bindData(colorProgram: ColorShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            colorProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT, 0
        )
    }

    fun draw() {
        for (drawCommand in drawList) {
            drawCommand.draw()
        }
    }

    companion object {
        private const val POSITION_COMPONENT_COUNT = 3
    }
}
