package com.ost.game.systems

import com.badlogic.gdx.Gdx
import com.ost.game.core.EventBus
import com.ost.game.core.Ev
import com.ost.game.data.GameState

/** Save: autosave, manual, slot, backup, versioning (disimpan di penyimpanan internal aplikasi). */
class SaveSystem(
    private val state: GameState,
    private val orders: OrderSystem,
    private val bus: EventBus
) {
    private val version = 1
    private var timer = 0f
    private fun path(slot: String) = "saves/$slot.txt"

    fun exists(slot: String): Boolean = Gdx.files.local(path(slot)).exists()

    fun save(slot: String = "auto") {
        val f = Gdx.files.local(path(slot))
        if (f.exists()) Gdx.files.local("saves/$slot.bak").writeString(f.readString(), false)

        val s = state.serialize()
        val sb = StringBuilder()
        sb.append("V").append(version).append("\n")
        sb.append(listOf(s.cash, s.reputation, s.day, s.timeOfDay, s.ordersDone,
            s.productsSold, s.loan, s.stage).joinToString("|")).append("\n")
        sb.append("INV:").append(s.inventory.entries.joinToString(",") { "${it.key}=${it.value}" }).append("\n")
        orders.serialize().forEach { sb.append("ORD:").append(it.joinToString("|")).append("\n") }

        f.writeString(sb.toString(), false)
        bus.emit(Ev.SAVED, slot)
    }

    fun load(slot: String = "auto"): Boolean {
        val f = Gdx.files.local(path(slot))
        if (!f.exists()) return false
        return try {
            val lines = f.readString().lineSequence().toList()
            if (lines.isEmpty() || !lines[0].startsWith("V")) return false
            // Migrasi versi bisa ditambahkan di sini.
            val p = lines[1].split("|")
            state.apply(com.ost.game.data.GameSnapshot(
                p[0].toLong(), p[1].toInt(), p[2].toInt(), p[3].toFloat(),
                mutableMapOf(), p[4].toInt(), p[5].toInt(), p[6].toLong(), p[7].toInt()
            ))
            lines.firstOrNull { it.startsWith("INV:") }?.let { line ->
                state.inventory.clear()
                line.removePrefix("INV:").split(",").filter { it.isNotBlank() }.forEach {
                    val kv = it.split("=")
                    if (kv.size == 2) state.inventory[kv[0]] = kv[1].toInt()
                }
            }
            orders.deserialize(lines.filter { it.startsWith("ORD:") }.map {
                it.removePrefix("ORD:").split("|").toTypedArray()
            })
            bus.emit(Ev.LOADED, slot)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun update(dt: Float, autosave: Boolean = true) {
        if (!autosave) return
        timer += dt
        if (timer >= 60f) { timer = 0f; save("auto") }
    }
}
