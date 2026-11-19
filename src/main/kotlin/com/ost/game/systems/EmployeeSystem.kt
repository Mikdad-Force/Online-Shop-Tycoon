package com.ost.game.systems

import com.badlogic.gdx.math.Vector3
import com.ost.game.core.EventBus
import com.ost.game.core.Ev
import com.ost.game.data.Catalog
import com.ost.game.data.GameState
import com.ost.game.world.ChibiCharacter

enum class Role(val id: String, val label: String, val hireCost: Long) {
    PICKER("picker", "Picker", 250_000),
    STOCKER("stocker", "Warehouse Staff", 220_000),
    PACKER("packer", "Packer", 280_000),
    MANAGER("manager", "Manager", 400_000)
}

class Employee(
    val role: Role,
    val character: ChibiCharacter,
    var phase: String = "IDLE",
    var task: String = "",
    var timer: Float = 1f,
    var carrying: String? = null,
    val home: Vector3,
    val target: Vector3 = Vector3()
)

/** NPC karyawan dengan AI task system: picker, stocker, packer, manager. */
class EmployeeSystem(
    private val state: GameState,
    private val inventory: InventorySystem,
    private val packing: PackingSystem,
    private val upgrades: UpgradeSystem,
    private val bus: EventBus
) {
    val employees = mutableListOf<Employee>()
    val autoPicked = mutableListOf<String>()

    fun hire(role: Role): Boolean {
        if (employees.any { it.role == role }) {
            bus.emit(Ev.NOTIFICATION, "${role.label} sudah ada")
            return false
        }
        if (!state.spend(role.hireCost)) {
            bus.emit(Ev.NOTIFICATION, "Kas tidak cukup untuk merekrut ${role.label}")
            return false
        }
        val idx = employees.size
        val color = listOf(0x3F51B5, 0x009688, 0xFF7043, 0x673AB7)[idx % 4]
        val home = Vector3(-3f + idx * 2f, 0f, 2f)
        val ch = ChibiCharacter(color, uniform = true).apply {
            position.set(home)
            anim = "IDLE"
        }
        employees.add(Employee(role, ch, home = home))
        updateSalary()
        bus.emit(Ev.NOTIFICATION, "${role.label} direkrut! -Rp ${role.hireCost}")
        return true
    }

    fun fire(role: Role) {
        val e = employees.firstOrNull { it.role == role } ?: return
        e.character.dispose()
        employees.remove(e)
        updateSalary()
        bus.emit(Ev.NOTIFICATION, "${role.label} diberhentikan")
    }

    private fun updateSalary() {
        state.employeeSalary = employees.sumOf { 40_000L }
    }

    fun render(batch: com.badlogic.gdx.graphics.g3d.ModelBatch) {
        employees.forEach { it.character.render(batch) }
    }

    fun update(dt: Float) {
        employees.forEach { e ->
            e.timer -= dt
            when (e.phase) {
                "IDLE" -> if (e.timer <= 0f) chooseTask(e)
                "TO_SHELF", "TO_TABLE", "HOME" -> if (move(e, dt)) arrive(e)
                "WORK" -> if (e.timer <= 0f) finishWork(e)
            }
            e.character.update(dt, e.phase in listOf("TO_SHELF", "TO_TABLE", "HOME"), false)
        }
    }

    private fun move(e: Employee, dt: Float): Boolean {
        val pos = e.character.position
        val dx = e.target.x - pos.x
        val dz = e.target.z - pos.z
        val dist = kotlin.math.sqrt(dx * dx + dz * dz)
        if (dist < .25f) return true
        val speed = 2.4f * dt
        pos.x += dx / dist * speed
        pos.z += dz / dist * speed
        e.character.facing = Math.toDegrees(kotlin.math.atan2(dx.toDouble(), dz.toDouble())).toFloat()
        return false
    }

    private fun chooseTask(e: Employee) {
        e.carrying = null
        when (e.role) {
            Role.PICKER -> {
                val id = Catalog.products.firstOrNull { inventory.stock(it.id) > 0 }?.id
                if (id != null && autoPicked.size < 6) {
                    e.carrying = id
                    e.task = "PICK"
                    e.target.set(6f, 0f, -3f)
                    e.phase = "TO_SHELF"
                } else e.timer = 2f
            }
            Role.STOCKER -> {
                e.task = "RESTOCK"
                e.target.set(6f, 0f, -6.5f)
                e.phase = "TO_SHELF"
            }
            Role.PACKER -> {
                e.task = "PACK"
                e.target.set(1.6f, 0f, 0f)
                e.phase = "TO_TABLE"
            }
            Role.MANAGER -> {
                e.task = "MANAGE"
                e.target.set(-6f, 0f, -2f)
                e.phase = "TO_TABLE"
            }
        }
    }

    private fun arrive(e: Employee) {
        e.phase = "WORK"
        e.timer = when (e.task) {
            "PICK" -> 1.4f
            "RESTOCK" -> 2.2f
            "PACK" -> 2.4f
            else -> 3f
        }
        e.character.anim = "PACK"
    }

    private fun finishWork(e: Employee) {
        when (e.task) {
            "PICK" -> {
                // Picker mengambil stok dari rak dan menuju meja packing
                if (inventory.take(e.carrying ?: "")) {
                    e.phase = "TO_TABLE"
                    e.target.set(1.6f, 0f, 0f)
                    e.character.anim = "CARRY"
                    return
                }
            }
            "RESTOCK" -> {
                val total = inventory.totalItems()
                if (total < state.storageCapacity) {
                    val p = Catalog.products.minByOrNull { inventory.stock(it.id) } ?: Catalog.products.first()
                    if (state.spend(p.buyPrice * 2)) {
                        inventory.returnStock(p.id, 3)
                        bus.emit(Ev.NOTIFICATION, "Stocker mengisi ulang 3x ${p.name}")
                    }
                }
            }
            "PACK" -> {
                if (packing.step >= 0) {
                    packing.interact()
                    if (upgrades.has("packbot")) packing.interact()
                }
            }
            "MANAGE" -> {
                state.addReputation(1)
                bus.emit(Ev.NOTIFICATION, "Manager: laporan harian selesai, reputasi +1")
            }
        }
        e.carrying = null
        e.character.anim = "IDLE"
        e.target.set(e.home)
        e.phase = "HOME"
    }

    fun takeAutoPicked(): String? {
        val id = autoPicked.removeFirstOrNull()
        if (id != null) bus.emit(Ev.NOTIFICATION, "Picker menyiapkan ${Catalog.find(id)?.name} di meja")
        return id
    }

    fun serialize(): String = employees.joinToString(",") { it.role.id }

    fun deserialize(s: String) {
        employees.forEach { it.character.dispose() }
        employees.clear()
        autoPicked.clear()
        s.split(",").filter { it.isNotBlank() }.forEach { id ->
            Role.entries.firstOrNull { it.id == id }?.let { role ->
                val idx = employees.size
                val color = listOf(0x3F51B5, 0x009688, 0xFF7043, 0x673AB7)[idx % 4]
                val home = Vector3(-3f + idx * 2f, 0f, 2f)
                val ch = ChibiCharacter(color, uniform = true).apply { position.set(home) }
                employees.add(Employee(role, ch, home = home))
            }
        }
        updateSalary()
    }
}
