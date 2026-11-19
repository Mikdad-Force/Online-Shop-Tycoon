package com.ost.game.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3
import kotlin.math.sin

/**
 * Karakter chibi prosedural (tanpa asset 3D eksternal).
 * Animasi dibuat dari rotasi/posisi tiap bagian = state machine visual.
 */
class ChibiCharacter(
    shirt: Int,
    private val skin: Int = 0xF1C7A0,
    private val hair: Int = 0x3E2723,
    private val uniform: Boolean = false
) {
    private val mb = ModelBuilder()
    private val models = mutableListOf<Model>()
    private val head: ModelInstance
    private val hairMesh: ModelInstance
    private val body: ModelInstance
    private val armL: ModelInstance
    private val armR: ModelInstance
    private val legL: ModelInstance
    private val legR: ModelInstance
    private val eyeL: ModelInstance
    private val eyeR: ModelInstance
    private val cap: ModelInstance?

    var position = Vector3()
    var facing = 0f // derajat
    var anim = "IDLE"
    var t = 0f
    var visible = true

    init {
        head = ball(.36f, skin, 10, 8)
        hairMesh = ball(.38f, hair, 10, 8)
        body = box(.62f, .66f, .4f, if (uniform) 0x2E8B8B else shirt)
        armL = box(.17f, .55f, .17f, if (uniform) 0x247474 else shirt)
        armR = box(.17f, .55f, .17f, if (uniform) 0x247474 else shirt)
        legL = box(.21f, .5f, .22f, 0x37474F)
        legR = box(.21f, .5f, .22f, 0x37474F)
        eyeL = box(.07f, .09f, .03f, 0x263238)
        eyeR = box(.07f, .09f, .03f, 0x263238)
        cap = if (uniform) ball(.39f, 0x1F6E6E, 10, 6) else null
    }

    private fun col(hex: Int): Color = Color(
        ((hex shr 16) and 0xFF) / 255f,
        ((hex shr 8) and 0xFF) / 255f,
        (hex and 0xFF) / 255f, 1f)

    private fun mat(hex: Int) = Material(ColorAttribute.createDiffuse(col(hex)))

    private fun box(w: Float, h: Float, d: Float, hex: Int): ModelInstance {
        val m = mb.createBox(w, h, d, mat(hex), ATTRS)
        models.add(m)
        return ModelInstance(m)
    }

    private fun ball(r: Float, hex: Int, seg: Int, rings: Int): ModelInstance {
        val m = mb.createSphere(r, r, r, seg, rings, mat(hex), ATTRS)
        models.add(m)
        return ModelInstance(m)
    }

    private fun place(
        inst: ModelInstance, x: Float, y: Float, z: Float,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f
    ) {
        val m = inst.transform
        m.setToTranslation(position)
        m.rotate(Vector3.Y, facing)
        m.translate(x, y, z)
        if (rx != 0f) m.rotate(Vector3.X, rx)
        if (ry != 0f) m.rotate(Vector3.Y, ry)
        if (rz != 0f) m.rotate(Vector3.Z, rz)
    }

    fun update(dt: Float, moving: Boolean, running: Boolean) {
        if (!visible) return
        t += dt
        val cycle = sin(t * (if (running) 13f else 8f))
        val amp = if (running) 1f else .55f
        val bob = if (moving) kotlin.math.abs(sin(t * (if (running) 13f else 8f))) * .05f
                  else sin(t * 2.2f) * .015f

        when (anim) {
            "WALK", "RUN" -> {
                place(legL, -.17f, .30f + bob, 0f, rx = cycle * 34f * amp)
                place(legR, .17f, .30f + bob, 0f, rx = -cycle * 34f * amp)
                place(armL, -.42f, .98f + bob, 0f, rx = -cycle * 30f * amp)
                place(armR, .42f, .98f + bob, 0f, rx = cycle * 30f * amp)
            }
            "CARRY" -> {
                place(legL, -.17f, .30f + bob, 0f)
                place(legR, .17f, .30f + bob, 0f)
                place(armL, -.42f, .95f, -.18f, rx = -78f)
                place(armR, .42f, .95f, -.18f, rx = -78f)
            }
            "PACK" -> {
                val w = sin(t * 9f) * 14f
                place(legL, -.17f, .30f, 0f); place(legR, .17f, .30f, 0f)
                place(armL, -.42f, .95f, -.15f, rx = -52f + w)
                place(armR, .42f, .95f, -.15f, rx = -52f - w)
            }
            "CELEBRATE" -> {
                val jump = kotlin.math.abs(sin(t * 7f)) * .25f
                place(legL, -.17f, .30f + jump, 0f)
                place(legR, .17f, .30f + jump, 0f)
                place(armL, -.45f, 1.05f + jump, 0f, rz = -150f)
                place(armR, .45f, 1.05f + jump, 0f, rz = 150f)
            }
            "TIRED" -> {
                place(legL, -.17f, .30f, 0f); place(legR, .17f, .30f, 0f)
                place(armL, -.42f, .92f, .1f, rx = 24f)
                place(armR, .42f, .92f, .1f, rx = 24f)
            }
            "TALK" -> {
                place(legL, -.17f, .30f, 0f); place(legR, .17f, .30f, 0f)
                place(armL, -.42f, .95f, 0f)
                place(armR, .42f, .98f, 0f, rz = -35f + sin(t * 6f) * 12f)
            }
            else -> {
                place(legL, -.17f, .30f, 0f); place(legR, .17f, .30f, 0f)
                place(armL, -.42f, .96f + bob, 0f, rz = -4f)
                place(armR, .42f, .96f + bob, 0f, rz = 4f)
            }
        }
        place(body, 0f, .74f + bob, 0f, rz = if (anim == "TIRED") 9f else 0f)
        place(head, 0f, 1.34f + bob, 0f, ry = sin(t * 1.5f) * 4f)
        place(hairMesh, 0f, 1.50f + bob, .02f, ry = sin(t * 1.5f) * 4f)
        place(eyeL, -.13f, 1.33f + bob, -.33f)
        place(eyeR, .13f, 1.33f + bob, -.33f)
        cap?.let { place(it, 0f, 1.58f + bob, .02f) }
    }

    fun render(batch: ModelBatch) {
        if (!visible) return
        listOf(legL, legR, body, armL, armR, head, hairMesh, eyeL, eyeR, cap)
            .filterNotNull().forEach { batch.render(it) }
    }

    fun dispose() {
        models.forEach { it.dispose() }
        models.clear()
    }

    companion object {
        private val ATTRS: Long = (Usage.Position or Usage.Normal).toLong()
    }
}
