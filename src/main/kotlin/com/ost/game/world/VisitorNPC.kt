package com.ost.game.world

import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.math.Vector3
import com.ost.game.core.Ev
import com.ost.game.core.GameContext

/** Pengunjung toko: datang, jalan ke counter, bicara, lalu pergi (Phase 7/17). */
class VisitorNPC {
    private val ch = ChibiCharacter(0xFFB74D)
    private var phase = "IDLE"
    private var timer = 0f
    var active = false
        private set
    private val entrance = Vector3(0f, 0f, 11f)
    private val counter = Vector3(-1.5f, 0f, 3.5f)

    fun spawn() {
        if (active) return
        active = true
        phase = "ENTER"
        ch.position.set(entrance)
        ch.anim = "WALK"
        GameContext.bus.emit(Ev.NOTIFICATION, "Ada pengunjung masuk ke toko")
    }

    fun update(dt: Float) {
        if (!active) return
        timer -= dt
        when (phase) {
            "ENTER" -> if (walkTo(counter, dt)) { phase = "TALK"; timer = 3.5f; ch.anim = "TALK" }
            "TALK" -> if (timer <= 0f) {
                GameContext.state.addReputation(1)
                GameContext.bus.emit(Ev.NOTIFICATION, "Pengunjung puas dengan pelayanan. Reputasi +1")
                phase = "LEAVE"; ch.anim = "WALK"
            }
            "LEAVE" -> if (walkTo(entrance, dt)) { active = false; phase = "IDLE" }
        }
        ch.update(dt, phase == "ENTER" || phase == "LEAVE", false)
    }

    private fun walkTo(target: Vector3, dt: Float): Boolean {
        val p = ch.position
        val dx = target.x - p.x
        val dz = target.z - p.z
        val d = kotlin.math.sqrt(dx * dx + dz * dz)
        if (d < .3f) return true
        p.x += dx / d * 2f * dt
        p.z += dz / d * 2f * dt
        ch.facing = Math.toDegrees(kotlin.math.atan2(dx.toDouble(), dz.toDouble())).toFloat()
        return false
    }

    fun render(batch: ModelBatch, env: com.badlogic.gdx.graphics.g3d.Environment) {
        if (active) ch.render(batch, env)
    }
    fun dispose() = ch.dispose()
}
