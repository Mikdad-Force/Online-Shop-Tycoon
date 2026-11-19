package com.ost.game.data

import com.ost.game.core.EventBus
import com.ost.game.core.Ev
import com.ost.game.core.GameConfig

/** State runtime satu instance. Semua sistem membaca/menulis lewat sini. */
class GameState(private val bus: EventBus) {
    var cash: Long = GameConfig.START_CASH
        private set
    var reputation: Int = 0
        private set
    var day: Int = 1
    var timeOfDay: Float = 0.35f // 0..1
    val inventory: MutableMap<String, Int> = mutableMapOf()
    val orders: MutableList<Order> = mutableListOf()
    var ordersDone: Int = 0
    var productsSold: Int = 0
    var loan: Long = 0
    var stage: Int = 1
    var rewardMultiplier: Float = 1f

    fun addCash(amount: Long) {
        cash += amount
        bus.emit(Ev.CASH_CHANGED, cash)
    }

    fun spend(amount: Long): Boolean {
        if (cash < amount) return false
        cash -= amount
        bus.emit(Ev.CASH_CHANGED, cash)
        return true
    }

    fun addReputation(amount: Int) {
        reputation = (reputation + amount).coerceIn(0, 100)
        bus.emit(Ev.REP_CHANGED, reputation)
    }

    fun nextOrderId(): Int = (orders.maxOfOrNull { it.id } ?: 0) + 1

    fun serialize(): GameSnapshot = GameSnapshot(
        cash, reputation, day, timeOfDay,
        HashMap(inventory), ordersDone, productsSold, loan, stage
    )

    fun apply(s: GameSnapshot) {
        cash = s.cash; reputation = s.reputation; day = s.day; timeOfDay = s.timeOfDay
        inventory.clear(); inventory.putAll(s.inventory)
        ordersDone = s.ordersDone; productsSold = s.productsSold; loan = s.loan; stage = s.stage
        bus.emit(Ev.CASH_CHANGED, cash)
        bus.emit(Ev.REP_CHANGED, reputation)
    }

    fun reset() {
        rewardMultiplier = 1f
        cash = GameConfig.START_CASH
        reputation = 0; day = 1; timeOfDay = 0.35f
        inventory.clear(); orders.clear()
        ordersDone = 0; productsSold = 0; loan = 0; stage = 1
    }
}
