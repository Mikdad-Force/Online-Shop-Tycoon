package com.ost.game.core

/** State machine generik untuk NPC, kurir, dan proses packing. */
class StateMachine(
    private val states: Map<String, State>,
    initial: String
) {
    interface State {
        fun enter(ctx: Any?, payload: Any?) {}
        fun update(ctx: Any?, dt: Float, elapsed: Float) {}
        fun exit(ctx: Any?) {}
    }

    var current: String = ""
        private set
    var elapsed: Float = 0f
        private set

    init {
        states[initial]?.enter(null, null)
        current = initial
    }

    fun change(name: String, ctx: Any? = null, payload: Any? = null) {
        if (name == current) return
        states[current]?.exit(ctx)
        current = name
        elapsed = 0f
        states[name]?.enter(ctx, payload)
    }

    fun update(dt: Float, ctx: Any? = null) {
        elapsed += dt
        states[current]?.update(ctx, dt, elapsed)
    }
}
