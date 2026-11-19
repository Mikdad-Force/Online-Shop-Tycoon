package com.ost.game.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.ost.game.core.GameContext

/** Conveyor bergerak + forklift mengangkut palet. */
class WarehouseFX {
    private val mb = ModelBuilder()
    private val models = mutableListOf<Model>()
    private val belt: ModelInstance
    private val boxes = mutableListOf<ModelInstance>()
    private val forklift: ModelInstance
    private val pallet: ModelInstance
    private var t = 0f
    var forkliftEnabled = false

    init {
        belt = box(6f, .25f, 1.2f, 0x546E7A).apply { transform.setToTranslation(4f, .75f, 6f) }
        repeat(5) { i ->
            boxes.add(box(.55f, .5f, .55f, listOf(0xC99A63, 0x90A4AE, 0x8D6E63)[i % 3]).apply {
                transform.setToTranslation(-1.8f + i * 1.8f, 1.12f, 6f)
            })
        }
        forklift = box(1.4f, 1f, 2f, 0xFBC02D)
        pallet = box(1.6f, .2f, 1.6f, 0x795548)
    }

    private fun col(hex: Int) = Color(
        ((hex shr 16) and 0xFF) / 255f, ((hex shr 8) and 0xFF) / 255f,
        (hex and 0xFF) / 255f, 1f)

    private fun box(w: Float, h: Float, d: Float, hex: Int): ModelInstance {
        val m = mb.createBox(w, h, d, Material(ColorAttribute.createDiffuse(col(hex))),
            (Usage.Position or Usage.Normal).toLong())
        models.add(m)
        return ModelInstance(m)
    }

    fun update(dt: Float) {
        t += dt
        boxes.forEach { b ->
            b.transform.getTranslation(tmp)
            tmp.x += 2.2f * dt
            if (tmp.x > 7f) tmp.x = .8f
            b.transform.setToTranslation(tmp)
            b.transform.rotate(com.badlogic.gdx.math.Vector3.Y, (t * 30f) % 360f)
        }
        // Forklift bolak-balik membawa palet
        val z = 4f + kotlin.math.sin(t * .5f) * 3f
        forklift.transform.setToTranslation(9f, .6f, z)
        forklift.transform.rotate(com.badlogic.gdx.math.Vector3.Y, if (kotlin.math.cos(t * .5f) > 0) 0f else 180f)
        pallet.transform.setToTranslation(9f, 1.15f, z + 1.2f)
    }

    private val tmp = com.badlogic.gdx.math.Vector3()

    fun render(batch: ModelBatch) {
        batch.render(belt)
        boxes.forEach { batch.render(it) }
        if (forkliftEnabled || GameContext.isReady() && GameContext.upgrades.has("forklift")) {
            batch.render(forklift)
            batch.render(pallet)
        }
    }

    fun dispose() {
        models.forEach { it.dispose() }
        models.clear()
    }
}
