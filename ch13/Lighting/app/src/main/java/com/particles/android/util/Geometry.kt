package com.particles.android.util

import kotlin.math.sqrt

sealed class Geometry {
    data class Point(
        val x: Float,
        val y: Float,
        val z: Float

    ): Geometry() {
        fun translateY(distance: Float): Point =
            Point(x, y + distance, z)

        fun translate(vector: Vector) : Point =
            Point(x + vector.x, y + vector.y, z + vector.z)
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

        fun normalize() =
            this * (1f / length)
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

    data class Sphere(
        val center: Point,
        val radius: Float
    )

    data class Plane(
        val point: Point,
        val normal: Vector
    )

    companion object {
        fun vectorBetween(from: Point, to: Point): Vector {
            return Vector(
                to.x - from.x,
                to.y - from.y,
                to.z - from.z
            )
        }

        // http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
        // Note that this formula treats Ray as if it extended infinitely past
        // either point.
        fun distanceBetween(point: Point, ray: Ray): Float {
            val p1ToPoint: Vector = vectorBetween(ray.point, point)
            val p2ToPoint: Vector = vectorBetween(ray.point.translate(ray.vector), point)

            // The length of the cross product gives the area of an imaginary
            // parallelogram having the two vectors as sides. A parallelogram can be
            // thought of as consisting of two triangles, so this is the same as
            // twice the area of the triangle defined by the two vectors.
            // http://en.wikipedia.org/wiki/Cross_product#Geometric_meaning
            val areaOfTriangleTimesTwo: Float = p1ToPoint.crossProduct(p2ToPoint).length
            val lengthOfBase: Float = ray.vector.length

            // The area of a triangle is also equal to (base * height) / 2. In
            // other words, the height is equal to (area * 2) / base. The height
            // of this triangle is the distance from the point to the ray.
            return areaOfTriangleTimesTwo / lengthOfBase
        }


        fun intersects(sphere: Sphere, ray: Ray): Boolean {
            return distanceBetween(sphere.center, ray) < sphere.radius
        }

        // http://en.wikipedia.org/wiki/Line-plane_intersection
        // This also treats rays as if they were infinite. It will return a
        // point full of NaNs if there is no intersection point.
        fun intersectionPoint(ray: Ray, plane: Plane): Point? {
            val rayToPlaneVector: Vector = vectorBetween(ray.point, plane.point)
            val scaleFactor: Float = (rayToPlaneVector * plane.normal
                    / (ray.vector * plane.normal))
            return ray.point.translate(ray.vector * scaleFactor)
        }


    }

}