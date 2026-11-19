package com.ost.game.systems

import com.ost.game.core.EventBus
import com.ost.game.core.Ev
import com.ost.game.data.GameState

enum class Upgrade(val id: String, val title: String, val cost: Long, val desc: String) {
    RACK2("rack2", "Rak Tambahan", 600_000, "+20 kapasitas penyimpanan"),
    PACKER_BOT("packbot", "Mesin Packing", 900_000, "Packing otomatis lebih cepat"),
    FORKLIFT("forklift", "Forklift Gudang", 1_500_000, "Restock cepat + animasi forklift"),
    VAN("van", "Armada Kurir", 2_000_000, "+20% hadiah setiap pengiriman"),
    AUTO_RESTOCK("autorest", "Auto Restock", 2_500_000, "Stocker bekerja tanpa digaji"),
    WAREHOUSE2("warehouse2", "Gudang Baru", 4_000_000, "+50 kapasitas & area baru")
}

/** Upgrade fasilitas. Efek langsung diterapkan ke state agar sistem lain bisa membacanya. */
class UpgradeSystem(private val state: GameState, private val bus: EventBus) {
    val owned = mutableSetOf<String>()

    fun has(id: String) = id in owned

    fun buy(u: Upgrade): Boolean {
        if (has(u.id)) {
            bus.emit(Ev.NOTIFICATION, "${u.title} sudah dimiliki")
            return false
        }
        if (!state.spend(u.cost)) {
            bus.emit(Ev.NOTIFICATION, "Kas tidak cukup untuk ${u.title} (Rp ${u.cost})")
            return false
        }
        owned.add(u.id)
        apply(u.id)
        bus.emit(Ev.NOTIFICATION, "Upgrade dibeli: ${u.title} — ${u.desc}")
        bus.emit(Ev.SETTINGS_CHANGED, u.id)
        return true
    }

    private fun apply(id: String) {
        when (id) {
            "rack2" -> state.storageCapacity += 20
            "warehouse2" -> state.storageCapacity += 50
        }
    }

    fun serialize(): String = owned.joinToString(",")

    fun deserialize(s: String) {
        owned.clear()
        s.split(",").filter { it.isNotBlank() }.forEach { owned.add(it) }
        owned.forEach(::apply)
    }
}
