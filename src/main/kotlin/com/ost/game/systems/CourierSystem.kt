package com.ost.game.systems

import com.ost.game.core.EventBus
import com.ost.game.core.Ev
import com.ost.game.core.StateMachine

/** Kurir + kendaraan: animasi datang, parkir, jalan, ambil paket, pergi. */
class CourierSystem(private val bus: EventBus) {
    var vanZ: Float = -32f
    var courierZ: Float = -32f
    var vanActive: Boolean = false
    var courierActive: Boolean = false

    private val sm: StateMachine = StateMachine(mapOf(
        "IDLE" to object : StateMachine.State {
            override fun enter(ctx: Any?, payload: Any?) {
                vanActive = false; courierActive = false
                vanZ = -32f; courierZ = -32f
            }
        },
        "COMING" to object : StateMachine.State {
            override fun enter(ctx: Any?, payload: Any?) {
                vanActive = true; bus.emit(Ev.COURIER, "Kurir sedang menuju toko...")
            }
            override fun update(ctx: Any?, dt: Float, elapsed: Float) {
                vanZ += 6f * dt
                if (vanZ >= -9f) sm.change("PARKED")
            }
        },
        "PARKED" to object : StateMachine.State {
            override fun update(ctx: Any?, dt: Float, elapsed: Float) {
                if (elapsed > 1.5f) sm.change("WALKING")
            }
        },
        "WALKING" to object : StateMachine.State {
            override fun enter(ctx: Any?, payload: Any?) { courierActive = true }
            override fun update(ctx: Any?, dt: Float, elapsed: Float) {
                courierZ += 2.2f * dt
                if (courierZ >= 2.5f) sm.change("LOADING")
            }
        },
        "LOADING" to object : StateMachine.State {
            override fun enter(ctx: Any?, payload: Any?) { bus.emit(Ev.COURIER, "Kurir memindai & memuat paket") }
            override fun update(ctx: Any?, dt: Float, elapsed: Float) {
                if (elapsed > 2.5f) sm.change("LEAVING")
            }
        },
        "LEAVING" to object : StateMachine.State {
            override fun update(ctx: Any?, dt: Float, elapsed: Float) {
                courierZ -= 3f * dt
                vanZ -= 6f * dt
                if (courierZ <= -32f && vanZ <= -32f) {
                    bus.emit(Ev.COURIER, "Kurir telah pergi. Paket dalam perjalanan!")
                    sm.change("IDLE")
                }
            }
        }
    ), "IDLE")

    val isIdle: Boolean get() = sm.current == "IDLE"

    fun call() {
        if (sm.current == "IDLE") sm.change("COMING")
    }

    fun update(dt: Float) = sm.update(dt)
}
