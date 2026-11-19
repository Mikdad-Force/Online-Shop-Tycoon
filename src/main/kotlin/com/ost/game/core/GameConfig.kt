package com.ost.game.core

object GameConfig {
    // Player
    const val WALK_SPEED = 3.6f
    const val RUN_SPEED = 6.4f
    const val JUMP_SPEED = 5.2f
    const val GRAVITY = 15f
    const val EYE_HEIGHT = 1.65f
    const val CROUCH_HEIGHT = 1.0f
    const val INTERACT_RANGE = 3.2f

    // Ekonomi
    const val START_CASH = 5_000_000L
    const val DAILY_RENT = 150_000L
    const val DAILY_SALARY = 100_000L

    // Waktu
    const val DAY_LENGTH = 240f // detik per hari game

    // Order
    const val ORDER_INTERVAL_MIN = 14f
    const val ORDER_INTERVAL_MAX = 26f
    const val ORDER_LIFETIME = 180f
}
