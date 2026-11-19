package com.ost.game.data

data class ProductDef(
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val buyPrice: Long,
    val sellPrice: Long,
    val color: Int
)

data class Order(
    val id: Int,
    val productId: String,
    val qty: Int,
    val reward: Long,
    var remaining: Float,
    var packed: Boolean = false
)

/** Snapshot data untuk save system. */
data class GameSnapshot(
    var cash: Long = 0,
    var reputation: Int = 0,
    var day: Int = 1,
    var timeOfDay: Float = 0.35f,
    var inventory: MutableMap<String, Int> = mutableMapOf(),
    var ordersDone: Int = 0,
    var productsSold: Int = 0,
    var loan: Long = 0,
    var stage: Int = 1,
    var storageCapacity: Int = 50
)

object Catalog {
    val products = listOf(
        ProductDef("kopi", "Kopi Susu", "KP-001", "Minuman", 8500, 18000, 0x8D5524),
        ProductDef("teh", "Teh Botol", "TH-002", "Minuman", 6000, 13000, 0xC8A05A),
        ProductDef("buku", "Buku Catatan", "BK-003", "Alat Tulis", 12000, 26000, 0x4A90D9),
        ProductDef("pensil", "Pensil Set", "PN-004", "Alat Tulis", 9000, 20000, 0xF2C94C),
        ProductDef("kaos", "Kaos Premium", "KS-005", "Fashion", 45000, 95000, 0xEB5757),
        ProductDef("tumbler", "Tumbler", "TM-006", "Home", 35000, 75000, 0x27AE60),
        ProductDef("headset", "Headset", "HS-007", "Elektronik", 120000, 245000, 0x9B51E0),
        ProductDef("lamp", "Lampu Meja", "LP-008", "Home", 75000, 160000, 0xF2994A)
    )

    fun find(id: String) = products.firstOrNull { it.id == id }

    fun stageName(stage: Int) = when (stage) {
        1 -> "Home Online Shop"
        2 -> "Small Online Store"
        3 -> "Professional Online Shop"
        4 -> "Medium Warehouse"
        5 -> "Distribution Center"
        6 -> "E-Commerce Corporation"
        else -> "International Business"
    }

    fun stageRequirement(stage: Int) = 5 * stage * stage // jumlah order untuk naik stage
}
