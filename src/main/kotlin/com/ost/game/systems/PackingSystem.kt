package com.ost.game.systems

import com.ost.game.core.EventBus
import com.ost.game.core.Ev
import com.ost.game.data.Catalog
import com.ost.game.data.GameState

/** Packing station - enam tahapan yang dipicu interaksi pemain. */
class PackingSystem(private val state: GameState, private val bus: EventBus) {
    val steps = listOf("Buka kardus", "Masukkan produk", "Bubble wrap", "Lakban", "Timbang", "Tempel resi")

    var step: Int = -1
        private set
    var activeProduct: String? = null
        private set
    var readyPackage: String? = null
        private set

    val label: String
        get() = if (step in steps.indices) steps[step] else "Siap untuk packing"

    fun begin(productId: String) {
        if (step >= 0 || readyPackage != null) return
        activeProduct = productId
        step = 0
        bus.emit(Ev.PACKING_STEP, "Mulai packing: ${Catalog.find(productId)?.name ?: productId}")
    }

    fun interact() {
        if (step < 0) {
            bus.emit(Ev.NOTIFICATION, "Ambil produk dari rak, lalu letakkan di meja packing")
            return
        }
        step++
        if (step >= steps.size) {
            val p = activeProduct
            readyPackage = p
            activeProduct = null
            step = -1
            bus.emit(Ev.PACKING_DONE, p)
            bus.emit(Ev.NOTIFICATION, "Paket selesai! Serahkan ke kurir di area pickup")
        } else {
            bus.emit(Ev.PACKING_STEP, "Packing: $label")
        }
    }

    fun takePackage(): String? {
        val p = readyPackage
        readyPackage = null
        return p
    }

    fun cancel() {
        if (activeProduct != null) state.inventory.put(activeProduct!!, (state.inventory[activeProduct] ?: 0) + 1)
        activeProduct = null
        step = -1
    }
}
