package com.ost.game.systems

import com.ost.game.core.EventBus
import com.ost.game.core.Ev
import com.ost.game.data.Catalog
import com.ost.game.data.GameState
import kotlin.random.Random

data class LiveEventInfo(val name: String, val remaining: Float, val total: Float)

/** Variasi gameplay: flash sale, pesanan darurat, komplain, maintenance, bulk deal. */
class LiveEventSystem(
    private val state: GameState,
    private val orders: OrderSystem,
    private val bus: EventBus
) {
    private var timer = 55f
    private var duration = 0f
    var current = ""
        private set

    fun update(dt: Float) {
        if (duration > 0f) {
            duration -= dt
            if (duration <= 0f) end()
            else bus.emit(Ev.LIVE_EVENT, LiveEventInfo(current, duration, DURATION))
        } else {
            timer -= dt
            if (timer <= 0f) {
                start()
                timer = 70f + Random.nextFloat() * 60f
            }
        }
    }

    private fun start() {
        when (Random.nextInt(5)) {
            0 -> {
                current = "FLASH SALE! Hadiah order x2"
                state.rewardMultiplier = 2f
                bus.emit(Ev.NOTIFICATION, "FLASH SALE! Semua pesanan memberi hadiah 2x selama 45 detik")
            }
            1 -> {
                current = "PESANAN DARURAT"
                orders.spawnEmergency()
            }
            2 -> {
                current = "KOMPLAIN PELANGGAN"
                state.addReputation(-4)
                bus.emit(Ev.NOTIFICATION, "Ada komplain pelanggan. Reputasi -4. Balas dengan baik di PC!")
            }
            3 -> {
                current = "MAINTENANCE GUDANG"
                if (state.spend(150_000)) bus.emit(Ev.NOTIFICATION, "Maintenance conveyor & rak: -Rp 150.000")
                else bus.emit(Ev.NOTIFICATION, "Maintenance ditunda, kas tidak cukup")
            }
            else -> {
                current = "BULK DEAL SUPPLIER"
                val p = Catalog.products.random()
                val cost = (p.buyPrice * 5 * .7f).toLong()
                if (state.spend(cost)) {
                    state.inventory[p.id] = (state.inventory[p.id] ?: 0) + 5
                    bus.emit(Ev.INVENTORY_CHANGED, p.id)
                    bus.emit(Ev.NOTIFICATION, "Bulk deal: +5 ${p.name} hanya Rp $cost")
                } else bus.emit(Ev.NOTIFICATION, "Bulk deal batal, kas tidak cukup")
            }
        }
        duration = DURATION
        bus.emit(Ev.LIVE_EVENT, LiveEventInfo(current, duration, DURATION))
    }

    private fun end() {
        state.rewardMultiplier = 1f
        duration = 0f
        current = ""
        bus.emit(Ev.LIVE_EVENT, LiveEventInfo("", 0f, DURATION))
    }

    companion object { private const val DURATION = 45f }
}
