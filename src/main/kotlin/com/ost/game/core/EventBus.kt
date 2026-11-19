package com.ost.game.core

/** EventBus - komunikasi antar sistem tanpa referensi langsung (event-driven). */
class EventBus {
    private val listeners = HashMap<String, MutableList<(Any?) -> Unit>>()

    fun on(event: String, fn: (Any?) -> Unit) {
        listeners.getOrPut(event) { mutableListOf() }.add(fn)
    }

    fun emit(event: String, payload: Any? = null) {
        listeners[event]?.toList()?.forEach {
            try { it(payload) } catch (e: Exception) { e.printStackTrace() }
        }
    }
}

object Ev {
    const val NOTIFICATION = "ui.notification"
    const val CASH_CHANGED = "economy.cash"
    const val REP_CHANGED = "economy.rep"
    const val DAY_CHANGED = "time.day"
    const val CLOCK = "time.clock"
    const val ORDER_ADDED = "order.added"
    const val ORDER_DONE = "order.done"
    const val ORDER_FAILED = "order.failed"
    const val INVENTORY_CHANGED = "inventory.changed"
    const val PACKING_STEP = "packing.step"
    const val PACKING_DONE = "packing.done"
    const val COURIER = "courier.event"
    const val SAVED = "save.done"
    const val LOADED = "save.loaded"
}
