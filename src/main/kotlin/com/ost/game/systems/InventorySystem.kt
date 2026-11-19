package com.ost.game.systems

import com.ost.game.core.EventBus
import com.ost.game.core.Ev
import com.ost.game.data.Catalog
import com.ost.game.data.GameState

/** Inventory + Supplier: pembelian stok, pengeluaran, stok opname. */
class InventorySystem(private val state: GameState, private val bus: EventBus) {

    fun stock(productId: String): Int = state.inventory[productId] ?: 0

    fun buy(productId: String, qty: Int): Boolean {
        val p = Catalog.find(productId) ?: return false
        if (qty <= 0) return false
        val cost = p.buyPrice * qty
        if (!state.spend(cost)) {
            bus.emit(Ev.NOTIFICATION, "Uang tidak cukup untuk beli $qty ${p.name}")
            return false
        }
        state.inventory[productId] = stock(productId) + qty
        bus.emit(Ev.INVENTORY_CHANGED, productId)
        bus.emit(Ev.NOTIFICATION, "+$qty ${p.name} masuk gudang (-Rp $cost)")
        return true
    }

    fun take(productId: String): Boolean {
        if (stock(productId) <= 0) return false
        state.inventory[productId] = stock(productId) - 1
        bus.emit(Ev.INVENTORY_CHANGED, productId)
        return true
    }

    fun returnStock(productId: String, qty: Int = 1) {
        state.inventory[productId] = stock(productId) + qty
        bus.emit(Ev.INVENTORY_CHANGED, productId)
    }

    fun totalItems(): Int = state.inventory.values.sum()
}
