package com.airhockey.android.util

sealed class Geometry {
    data class Point(
        val x: Float,
        val y: Float,
        val z: Float

    ): Geometry() {
        fun translateY(distance: Float): Point =
            Point(x, y + distance, z)
    }

    data class Circle(
        val center: Point,
        val radius: Float
    ) {
        fun scale(scale: Float) =
            Circle(center, radius * scale)
    }

    data class Cylinder(
        val center: Point,
        val radius: Float,
        val height: Float
    ) : Geometry()
}