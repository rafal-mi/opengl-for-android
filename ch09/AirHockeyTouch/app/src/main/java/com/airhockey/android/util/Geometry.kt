package com.airhockey.android.util

import java.util.*
import kotlin.math.sqrt

sealed class Geometry {
    data class Point(
        val x: Float,
        val y: Float,
        val z: Float

    ): Geometry() {
        fun translateY(distance: Float): Point =
            Point(x, y + distance, z)
    }

    data class Vector(
        val x: Float,
        val y: Float,
        val z: Float
    ) {
        val length: Float
            get() = sqrt(x * x  +  y * y  +  z * z)

        fun crossProduct(other: Vector): Vector = Vector(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
            )

        operator fun times(other: Vector): Float =
            x * other.x + y * other.y + z * other.z

        operator fun times(f: Float): Vector =
            Vector(x * f, y * f, z * f)

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

    data class Ray(
        val point: Point,
        val vector: Vector
    )
}