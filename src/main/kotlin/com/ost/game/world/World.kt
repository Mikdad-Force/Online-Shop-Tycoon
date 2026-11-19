package com.ost.game.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.environment.PointLight
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3
import com.ost.game.core.GameContext
import com.ost.game.data.Catalog

/** Dunia 3D stylized: toko, gudang, area luar, properti, NPC, kendaraan. */
class World {
    private val mb = ModelBuilder()
    private val models = mutableListOf<Model>()
    private val statics = mutableListOf<ModelInstance>()
    private val productModels = HashMap<String, Model>()
    private val shelfDisplays = mutableListOf<Pair<String, ModelInstance>>()
    private val hiddenProducts = mutableSetOf<String>()
    private val expansion = mutableListOf<ModelInstance>()
    private var heldYaw = 0f

    val environment = Environment().apply {
        set(ColorAttribute.createAmbientLight(0.55f, 0.55f, 0.58f, 1f))
        add(PointLight().set(1f, .92f, .78f, 0f, 6f, -2f, 14f))
    }
    val sun = DirectionalLight().set(1f, .96f, .86f, .4f, 1f, .3f)

    val interactables = mutableListOf<Interactable>()

    private lateinit var van: ModelInstance
    private lateinit var courier: ModelInstance
    private lateinit var staff: ModelInstance
    private var held: ModelInstance? = null
    private var heldId: String? = null
    private var staffT = 0f

    private val tablePos = Vector3(0f, 1.02f, 0f)
    private val pickupPos = Vector3(0f, 0.2f, 5f)

    fun build() {
        environment.add(sun)
        val white = 0xF2F2F0
        val wood = 0xB98B5E
        val woodDark = 0x8B623F
        val teal = 0x2E8B8B

        // Lantai & dinding
        addBox(40f, .2f, 40f, 0xE8E6DF, 0f, -.1f, 0f)
        addBox(40f, 7f, .3f, white, 0f, 3.5f, -10f)
        addBox(.3f, 7f, 40f, white, -12f, 3.5f, 0f)
        addBox(.3f, 7f, 40f, white, 12f, 3.5f, 0f)
        addBox(40f, 7f, .3f, 0xDDE7EE, 0f, 3.5f, 12f)
        addBox(9f, .04f, 9f, 0xD6E8DA, 0f, .01f, 2f)
        addBox(5f, .04f, 2.5f, 0xF6E2B8, 0f, .02f, 5f)

        // Meja packing
        addBox(3.2f, .15f, 1.8f, wood, 0f, .95f, 0f)
        listOf(-1.4f, 1.4f).forEach { x -> listOf(-.7f, .7f).forEach { z ->
            addBox(.14f, .95f, .14f, woodDark, x, .48f, z)
        } }
        addBox(.9f, .6f, .7f, 0xC99A63, -.7f, 1.32f, 0f)
        addBox(.5f, .18f, .4f, 0x37474F, .6f, 1.11f, -.35f)
        addBox(.7f, .12f, .6f, 0x546E7A, 1.1f, 1.07f, .1f)

        // Meja komputer
        addBox(2.6f, .14f, 1.3f, wood, -7f, .9f, -3f)
        listOf(-8.1f, -5.9f).forEach { x -> listOf(-3.5f, -2.5f).forEach { z ->
            addBox(.12f, .9f, .12f, woodDark, x, .45f, z)
        } }
        addBox(1.3f, .85f, .08f, 0x263238, -7f, 1.6f, -3.4f)
        addBox(1.15f, .7f, .02f, teal, -7f, 1.6f, -3.34f)
        addBox(.5f, .03f, .2f, 0xCFD8DC, -7f, 1.06f, -2.8f)

        // Rak gudang
        fun rack(x: Float, z: Float) {
            listOf(-1.3f, 1.3f).forEach { dx -> listOf(-.55f, .55f).forEach { dz ->
                addBox(.12f, 2.6f, .12f, 0x607D8B, x + dx, 1.3f, z + dz)
            } }
            listOf(.5f, 1.3f, 2.1f).forEach { y ->
                addBox(2.8f, .1f, 1.3f, 0x90A4AE, x, y, z)
            }
        }
        rack(6f, -3f); rack(6f, -6.5f); rack(6f, 2f)

        // Area gudang tambahan (muncul setelah upgrade warehouse2)
        listOf(-6f, -2f, 5f).forEach { z -> expansionRack(-8.5f, z) }

        // Etalase depan
        listOf(.6f, 1.2f, 1.8f).forEach { y -> addBox(3f, .1f, .8f, 0xA1887F, -4f, y, 3f) }

        // Pintu & area pickup
        addBox(3f, 4f, .2f, 0x795548, 0f, 2f, -9.85f)
        addBox(.2f, 4f, .3f, 0x5D4037, -1.5f, 2f, -9.7f)
        addBox(.2f, 4f, .3f, 0x5D4037, 1.5f, 2f, -9.7f)
        addBox(1.6f, .06f, 1.6f, 0x47A86A, 0f, .05f, 5f)

        // Jalan & trotoar (luar)
        addBox(40f, .05f, 8f, 0x616A6B, 0f, -.03f, 17f)
        addBox(40f, .08f, 1f, 0xB0BEC5, 0f, .02f, 12.5f)

        // Kendaraan & karakter dinamis
        van = ModelInstance(makeModel(2.4f, 2.2f, 4.6f, 0x2E7D32))
        van.transform.setToTranslation(0f, 1.15f, -32f)
        courier = ModelInstance(makeModel(.7f, 1.7f, .5f, 0xE65153))
        staff = ModelInstance(makeModel(.7f, 1.7f, .5f, 0x3F51B5))

        buildShelfDisplays()
        buildInteractables()
    }

    private fun expansionRack(x: Float, z: Float) {
        listOf(-1.3f, 1.3f).forEach { dx -> listOf(-.55f, .55f).forEach { dz ->
            val i = ModelInstance(makeModel(.12f, 2.6f, .12f, 0x607D8B))
            i.transform.setToTranslation(x + dx, 1.3f, z + dz)
            expansion.add(i)
        } }
        listOf(.5f, 1.3f, 2.1f).forEach { y ->
            val i = ModelInstance(makeModel(2.8f, .1f, 1.3f, 0x90A4AE))
            i.transform.setToTranslation(x, y, z)
            expansion.add(i)
        }
    }

    private fun buildShelfDisplays() {
        val slots = listOf(
            Vector3(5f, .78f, -3f), Vector3(6.6f, .78f, -3f), Vector3(5f, 1.58f, -3f),
            Vector3(6.6f, 1.58f, -3f), Vector3(5f, .78f, -6.5f), Vector3(6.6f, .78f, -6.5f),
            Vector3(5f, 1.58f, -6.5f), Vector3(6.6f, 1.58f, -6.5f)
        )
        Catalog.products.forEachIndexed { i, p ->
            val inst = ModelInstance(makeModel(.4f, .5f, .4f, p.color))
            inst.transform.setToTranslation(slots[i % slots.size])
            shelfDisplays.add(p.id to inst)
        }
    }

    private fun buildInteractables() {
        interactables.add(Interactable("shelf", "Ambil produk dari rak", Vector3(6f, 1f, -3f), 2.8f))
        interactables.add(Interactable("table", "Letakkan di meja packing", tablePos, 1.8f))
        interactables.add(Interactable("packing", "Lanjutkan packing", Vector3(.7f, 1.1f, .2f), 1.6f))
        interactables.add(Interactable("computer", "Gunakan komputer", Vector3(-7f, 1.2f, -3f), 2.2f))
        interactables.add(Interactable("pickup", "Serahkan ke kurir", pickupPos, 2.4f))
        interactables.add(Interactable("door", "Keluar toko", Vector3(0f, 1f, -9f), 2f))
    }

    private fun addBox(w: Float, h: Float, d: Float, color: Int, x: Float, y: Float, z: Float): ModelInstance {
        val inst = ModelInstance(makeModel(w, h, d, color))
        inst.transform.setToTranslation(x, y, z)
        statics.add(inst)
        return inst
    }

    private fun makeModel(w: Float, h: Float, d: Float, color: Int): Model {
        val model = mb.createBox(w, h, d, Material(ColorAttribute.createDiffuse(col(color))),
            ATTRS)
        models.add(model)
        return model
    }

    private fun productModel(id: String): Model {
        productModels[id]?.let { return it }
        val p = Catalog.find(id)
        val m = mb.createBox(.34f, .34f, .34f,
            Material(ColorAttribute.createDiffuse(col(p?.color ?: 0xCCCCCC))), ATTRS)
        models.add(m)
        productModels[id] = m
        return m
    }

    private fun col(hex: Int): Color = Color(
        ((hex shr 16) and 0xFF) / 255f,
        ((hex shr 8) and 0xFF) / 255f,
        (hex and 0xFF) / 255f,
        1f
    )

    fun setHeld(id: String?) {
        heldId = id
        heldYaw = 0f
        held = if (id == null) null else ModelInstance(productModel(id))
    }

    fun rotateHeld() { heldYaw = (heldYaw + 45f) % 360f }

    fun getHeld(): String? = heldId

    fun update(dt: Float, camera: com.badlogic.gdx.graphics.Camera, player: Player) {
        hiddenProducts.clear()
        Catalog.products.forEach { p ->
            if ((GameContext.state.inventory[p.id] ?: 0) <= 0) hiddenProducts.add(p.id)
        }

        held?.let { inst ->
            val f = camera.direction
            inst.transform.setToTranslation(
                camera.position.x + f.x * 1.1f,
                camera.position.y + f.y * 1.1f - .35f,
                camera.position.z + f.z * 1.1f
            )
            inst.transform.rotate(Vector3.Y, heldYaw)
        }

        val c = GameContext.courier
        van.transform.setToTranslation(0f, 1.15f, c.vanZ)
        courier.transform.setToTranslation(1.4f, .85f + bob(c.courierZ), c.courierZ)

        staffT += dt
        val sway = kotlin.math.sin(staffT * .8f) * 3f
        staff.transform.setToTranslation(-3f + sway, .85f + bob(staffT * 2f), 2f)
    }

    private fun bob(t: Float): Float = kotlin.math.sin(t * 6f) * .04f

    fun render(batch: ModelBatch) {
        batch.render(statics)
        if (GameContext.isReady() && GameContext.upgrades.has("warehouse2")) batch.render(expansion)
        shelfDisplays.forEach { (id, inst) -> if (id !in hiddenProducts) batch.render(inst) }
        held?.let { batch.render(it) }
        if (GameContext.courier.vanActive) batch.render(van)
        if (GameContext.courier.courierActive) batch.render(courier)
        batch.render(staff)
    }

    fun updateLighting(sunX: Float, sunY: Float) {
        sun.set(1f, .96f, .86f, sunX * .6f, sunY.coerceAtLeast(.15f), sunX * .3f)
    }

    fun tablePosition(): Vector3 = tablePos
    fun pickupPosition(): Vector3 = pickupPos

    fun dispose() {
        models.forEach { it.dispose() }
        models.clear()
        productModels.clear()
    }

    companion object {
        private val ATTRS: Long = (Usage.Position or Usage.Normal).toLong()
    }
}
