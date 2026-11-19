package com.ost.game.core

import com.ost.game.data.GameState
import com.ost.game.systems.*
import com.ost.game.world.Player
import com.ost.game.world.World

/** Container dependensi sederhana (Service Locator). Diinisialisasi sekali saat game dibuat. */
object GameContext {
    lateinit var bus: EventBus
        private set
    lateinit var state: GameState
        private set
    lateinit var inventory: InventorySystem
        private set
    lateinit var orders: OrderSystem
        private set
    lateinit var packing: PackingSystem
        private set
    lateinit var courier: CourierSystem
        private set
    lateinit var time: DayNightSystem
        private set
    lateinit var save: SaveSystem
        private set
    lateinit var audio: AudioSystem
        private set
    lateinit var events: LiveEventSystem
        private set
    lateinit var upgrades: UpgradeSystem
        private set
    lateinit var employees: EmployeeSystem
        private set
    lateinit var world: World
        private set
    lateinit var player: Player
        private set

    var initialized = false
        private set

    fun init(world: World, player: Player) {
        if (initialized) return
        this.world = world
        this.player = player
        bus = EventBus()
        Settings.load()
        state = GameState(bus)
        inventory = InventorySystem(state, bus)
        orders = OrderSystem(state, bus)
        packing = PackingSystem(state, bus)
        courier = CourierSystem(bus)
        time = DayNightSystem(state, bus)
        upgrades = UpgradeSystem(state, bus)
        employees = EmployeeSystem(state, inventory, packing, upgrades, bus)
        save = SaveSystem(state, orders, upgrades, employees, bus)
        audio = AudioSystem(bus)
        events = LiveEventSystem(state, orders, bus)
        initialized = true
    }

    fun isReady(): Boolean = initialized
}
