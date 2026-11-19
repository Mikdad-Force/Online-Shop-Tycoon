package com.ost.game.world

import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.Vector3
import com.ost.game.core.GameConfig
import com.ost.game.core.Settings
import kotlin.math.cos
import kotlin.math.sin

/** Player first-person: jalan, lari, lompat, jongkok, lihat (touch/keyboard). */
class Player(val camera: PerspectiveCamera) {
    val position = Vector3(0f, GameConfig.EYE_HEIGHT, 8f)
    var yaw = 0f
    var pitch = 0f
    private var velocityY = 0f
    private var grounded = true
    var crouching = false
        private set
    var sprinting = false
        private set

    private val forward = Vector3()
    private val right = Vector3()

    fun update(
        dt: Float,
        moveX: Float, moveY: Float,
        lookDx: Float, lookDy: Float,
        jump: Boolean, wantCrouch: Boolean, wantSprint: Boolean
    ) {
        val sens = 0.0045f * Settings.sensitivity
        yaw += lookDx * sens
        pitch = (pitch - lookDy * sens).coerceIn(-1.3f, 1.3f)

        crouching = wantCrouch
        sprinting = wantSprint && !crouching && (moveX != 0f || moveY != 0f)
        val speed = if (crouching) GameConfig.WALK_SPEED * .5f
                    else if (sprinting) GameConfig.RUN_SPEED else GameConfig.WALK_SPEED

        forward.set(sin(yaw), 0f, -cos(yaw)).nor()
        right.set(-forward.z, 0f, forward.x).nor()

        val dx = (forward.x * moveY + right.x * moveX) * speed * dt
        val dz = (forward.z * moveY + right.z * moveX) * speed * dt
        val nx = (position.x + dx).coerceIn(-11f, 11f)
        val nz = (position.z + dz).coerceIn(-9f, 11f)
        position.x = nx
        position.z = nz

        if (jump && grounded) {
            velocityY = GameConfig.JUMP_SPEED
            grounded = false
        }
        if (!grounded) {
            velocityY -= GameConfig.GRAVITY * dt
            position.y += velocityY * dt
            val floor = if (crouching) GameConfig.CROUCH_HEIGHT else GameConfig.EYE_HEIGHT
            if (position.y <= floor) {
                position.y = floor
                velocityY = 0f
                grounded = true
            }
        } else {
            position.y = if (crouching) GameConfig.CROUCH_HEIGHT else GameConfig.EYE_HEIGHT
        }

        camera.position.set(position)
        camera.lookAt(
            position.x + forward.x * cos(pitch),
            position.y + sin(pitch),
            position.z + forward.z * cos(pitch)
        )
        camera.update()
    }
}
