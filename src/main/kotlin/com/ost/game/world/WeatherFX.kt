package com.ost.game.world

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlin.random.Random

/** Efek hujan 3D sederhana (garis jatuh) mengikuti cuaca hari ini. */
class WeatherFX {
    private data class Drop(var x: Float, var y: Float, var z: Float, var speed: Float)
    private val drops = List(when (com.ost.game.core.Settings.quality) { 0 -> 60; 1 -> 160; else -> 300 }) {
        Drop(Random.nextFloat() * 24 - 12, Random.nextFloat() * 12,
            Random.nextFloat() * 24 - 12, 8f + Random.nextFloat() * 6f)
    }
    private val sr = ShapeRenderer()

    fun update(dt: Float, camera: Camera, rain: Boolean) {
        if (!rain) return
        drops.forEach { d ->
            d.y -= d.speed * dt
            if (d.y < 0f) {
                d.y = 12f
                d.x = camera.position.x + Random.nextFloat() * 24 - 12
                d.z = camera.position.z + Random.nextFloat() * 24 - 12
            }
        }
    }

    fun render(camera: Camera, rain: Boolean) {
        if (!rain) return
        sr.projectionMatrix = camera.combined
        sr.begin(ShapeRenderer.ShapeType.Line)
        sr.color = Color(0.72f, 0.84f, 0.95f, .55f)
        drops.forEach { d -> sr.line(d.x, d.y, d.z, d.x + .12f, d.y - .55f, d.z) }
        sr.end()
    }

    fun dispose() = sr.dispose()
}
