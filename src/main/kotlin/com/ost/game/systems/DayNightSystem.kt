package com.ost.game.systems

import com.badlogic.gdx.graphics.Color
import com.ost.game.core.EventBus
import com.ost.game.core.Ev
import com.ost.game.core.GameConfig
import com.ost.game.data.GameState
import kotlin.math.cos
import kotlin.math.sin

/** Siklus siang-malam, cuaca, dan biaya harian. */
class DayNightSystem(private val state: GameState, private val bus: EventBus) {
    val sky = Color(0.53f, 0.81f, 0.92f, 1f)
    var sunX = 0f
    var sunY = 1f
    var rain = false
        private set

    fun update(dt: Float) {
        state.timeOfDay += dt / GameConfig.DAY_LENGTH
        if (state.timeOfDay >= 1f) {
            state.timeOfDay -= 1f
            state.day++
            val cost = GameConfig.DAILY_RENT + GameConfig.DAILY_SALARY
            state.spend(cost)
            bus.emit(Ev.DAY_CHANGED, state.day)
            bus.emit(Ev.NOTIFICATION, "Hari ${state.day} — biaya operasional -Rp $cost")
        }
        val t = state.timeOfDay
        val angle = (t - 0.25f) * (2f * Math.PI.toFloat())
        sunX = cos(angle)
        sunY = sin(angle)

        when {
            t < 0.22f -> sky.set(0.06f, 0.09f, 0.20f, 1f)
            t < 0.30f -> sky.set(0.95f, 0.55f, 0.35f, 1f)
            t < 0.70f -> sky.set(0.53f, 0.81f, 0.92f, 1f)
            t < 0.80f -> sky.set(0.96f, 0.60f, 0.36f, 1f)
            else -> sky.set(0.06f, 0.09f, 0.20f, 1f)
        }
        rain = (state.day % 5 == 0)
        bus.emit(Ev.CLOCK, t)
    }

    fun phaseName(): String = when {
        state.timeOfDay < 0.22f -> "Malam"
        state.timeOfDay < 0.30f -> "Pagi"
        state.timeOfDay < 0.55f -> "Siang"
        state.timeOfDay < 0.70f -> "Sore"
        state.timeOfDay < 0.80f -> "Senja"
        else -> "Malam"
    }

    fun clockText(): String {
        val totalMinutes = (state.timeOfDay * 24f * 60f).toInt()
        return "%02d:%02d".format(totalMinutes / 60, totalMinutes % 60)
    }
}
