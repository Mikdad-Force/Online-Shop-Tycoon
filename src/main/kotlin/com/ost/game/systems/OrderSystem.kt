package com.ost.game.systems

import com.ost.game.core.EventBus
import com.ost.game.core.Ev
import com.ost.game.core.GameConfig
import com.ost.game.data.Catalog
import com.ost.game.data.GameState
import com.ost.game.data.Order
import kotlin.random.Random

/** Marketplace + Order: generate pesanan, hitung deadline, selesaikan / gagal. */
class OrderSystem(private val state: GameState, private val bus: EventBus) {
    private var timer: Float = 6f

    fun update(dt: Float) {
        updateTimers(dt)
        timer -= dt
        if (timer <= 0f) {
            spawn()
            timer = GameConfig.ORDER_INTERVAL_MIN + Random.nextFloat() *
                (GameConfig.ORDER_INTERVAL_MAX - GameConfig.ORDER_INTERVAL_MIN)
        }
        val expired = state.orders.filter { it.remaining <= 0f }
        expired.forEach { fail(it) }
        state.orders.removeAll(expired)
    }

    private fun spawn() {
        if (state.orders.size >= 6) return
        val pool = Catalog.products.filter { state.stage >= 1 || it.id == "kopi" }
        val p = pool.random()
        val qty = if (state.stage >= 4 && Random.nextBoolean()) 2 else 1
        val reward = (p.sellPrice * qty * (1f + state.reputation / 400f) * state.rewardMultiplier).toLong()
        val order = Order(state.nextOrderId(), p.id, qty, reward, GameConfig.ORDER_LIFETIME)
        state.orders.add(order)
        bus.emit(Ev.ORDER_ADDED, order)
        bus.emit(Ev.NOTIFICATION, "Pesanan baru #${order.id}: ${qty}x ${p.name}")
    }

    /** Pesanan darurat: deadline pendek, hadiah 3x. Dipicu live event. */
    fun spawnEmergency() {
        if (state.orders.size >= 8) return
        val p = Catalog.products.random()
        val reward = (p.sellPrice * 3f).toLong()
        val order = Order(state.nextOrderId(), p.id, 1, reward, 75f)
        state.orders.add(order)
        bus.emit(Ev.ORDER_ADDED, order)
        bus.emit(Ev.NOTIFICATION, "PESANAN DARURAT #${order.id}: ${p.name}, 75 detik, Rp $reward!")
    }

    fun pendingFor(productId: String): Order? =
        state.orders.firstOrNull { it.productId == productId && !it.packed }

    fun complete(order: Order) {
        if (!state.orders.contains(order)) return
        state.orders.remove(order)
        state.addCash(order.reward)
        state.addReputation(3)
        state.ordersDone++
        state.productsSold += order.qty
        checkStage()
        bus.emit(Ev.ORDER_DONE, order)
        bus.emit(Ev.NOTIFICATION, "Pesanan #${order.id} selesai! +Rp ${order.reward}")
    }

    private fun fail(order: Order) {
        state.orders.remove(order)
        state.addReputation(-5)
        bus.emit(Ev.ORDER_FAILED, order)
        bus.emit(Ev.NOTIFICATION, "Pesanan #${order.id} kedaluwarsa. Reputasi -5")
    }

    private fun checkStage() {
        val need = Catalog.stageRequirement(state.stage)
        if (state.ordersDone >= need && state.stage < 7) {
            state.stage++
            state.addCash(1_000_000L * state.stage)
            bus.emit(Ev.NOTIFICATION, "NAIK STAGE ${state.stage}: ${Catalog.stageName(state.stage)}!")
        }
    }

    fun updateTimers(dt: Float) {
        state.orders.forEach { it.remaining -= dt }
    }

    fun serialize(): List<Array<String>> = state.orders.map {
        arrayOf(it.id.toString(), it.productId, it.qty.toString(), it.reward.toString(), it.remaining.toString())
    }

    fun deserialize(list: List<Array<String>>) {
        state.orders.clear()
        list.forEach {
            state.orders.add(Order(it[0].toInt(), it[1], it[2].toInt(), it[3].toLong(), it[4].toFloat()))
        }
    }
}
