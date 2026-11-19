package com.ost.game.world

import com.badlogic.gdx.math.Vector3

/** Objek yang bisa berinteraksi. Sistem interaksi memakai posisi + radius, modular. */
class Interactable(
    val id: String,
    val label: String,
    val position: Vector3,
    val radius: Float = 1.5f
) {
    var action: () -> Unit = {}
    var enabled: Boolean = true
}
