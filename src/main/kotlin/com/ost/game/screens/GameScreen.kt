package com.ost.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.badlogic.gdx.utils.Scaling
import com.ost.game.core.Ev
import com.ost.game.core.GameConfig
import com.ost.game.core.GameContext
import com.ost.game.data.Catalog
import com.ost.game.ui.ComputerUI
import com.ost.game.ui.HUD
import com.ost.game.ui.MainMenu
import com.ost.game.ui.PhoneUI
import com.ost.game.ui.UIStyles
import com.ost.game.core.Settings
import com.ost.game.world.Interactable
import com.ost.game.world.Player
import com.ost.game.world.World

/** Screen utama: menghubungkan input, world, sistem, dan UI. */
class GameScreen : ScreenAdapter() {
    private val camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
        position.set(0f, 1.65f, 8f)
        far = 200f
        update()
    }
    private val batch = ModelBatch()
    private val world = World()
    private val weather = com.ost.game.world.WeatherFX()
    private val warehouseFX = com.ost.game.world.WarehouseFX()
    private val player = Player(camera)
    private val avatar = com.ost.game.world.ChibiCharacter(0x26A69A)
    private val visitor = com.ost.game.world.VisitorNPC()
    private var visitorTimer = 45f
    private var stepTimer = 0f
    private var moving = false
    private var celebrateTimer = 0f

    private val skin: Skin = UIStyles.createSkin()
    // Viewport virtual 1280x720: UI tetap proporsional di HP resolusi berapa pun.
    private val stage = Stage(ScalingViewport(Scaling.fit, 1280f, 720f))
    private lateinit var hud: HUD
    private lateinit var phone: PhoneUI
    private lateinit var computer: ComputerUI
    private lateinit var menu: MainMenu

    private var lookDx = 0f
    private var lookDy = 0f
    private var jumpQueued = false
    private var menuVisible = true
    private var running = false

    private val ray = com.badlogic.gdx.math.collision.Ray()
    private val tmp = Vector3()
    private val closest = Vector3()
    private var currentTarget: Interactable? = null

    init {
        world.build()
        GameContext.init(world, player)
        GameContext.audio.load()
        GameContext.audio.startMusic()
        wireInteractables()
        GameContext.bus.on(Ev.ORDER_DONE) { celebrateTimer = 2.5f }

        hud = HUD(stage, skin, ::performInteract, { phone.toggle() }, { computer.toggle() }, {
            GameContext.save.save("manual")
            GameContext.bus.emit(Ev.NOTIFICATION, "Game tersimpan (slot manual)")
        }, {
            player.thirdPerson = !player.thirdPerson
            avatar.visible = player.thirdPerson
            GameContext.bus.emit(Ev.NOTIFICATION, if (player.thirdPerson) "Kamera: orang ketiga" else "Kamera: orang pertama")
        })
        phone = PhoneUI(skin)
        computer = ComputerUI(skin)
        menu = MainMenu(skin, { startNew() }, { startContinue() })

        stage.addActor(phone.window)
        stage.addActor(computer.window)
        stage.addActor(menu.root)
        phone.window.setPosition((stage.width - phone.window.width) / 2, 120f)
        computer.window.setPosition((stage.width - computer.window.width) / 2, 90f)

        val input = InputMultiplexer()
        input.addProcessor(stage)
        input.addProcessor(object : InputAdapter() {
            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                if (!menuVisible) {
                    lookDx += Gdx.input.deltaX.toFloat()
                    lookDy += Gdx.input.deltaY.toFloat()
                }
                return false
            }
            override fun keyDown(key: Int): Boolean {
                when (key) {
                    Input.Keys.E -> if (!menuVisible) performInteract()
                    Input.Keys.P -> if (!menuVisible) phone.toggle()
                    Input.Keys.C -> if (!menuVisible) computer.toggle()
                    Input.Keys.V -> if (!menuVisible) {
                        player.thirdPerson = !player.thirdPerson
                        avatar.visible = player.thirdPerson
                    }
                    Input.Keys.R -> if (!menuVisible) world.rotateHeld()
                    Input.Keys.ESCAPE -> { phone.hide(); computer.hide() }
                }
                return false
            }
        })
        Gdx.input.inputProcessor = input
    }

    private fun wireInteractables() {
        val inv = GameContext.inventory
        val pack = GameContext.packing
        val ord = GameContext.orders
        val courier = GameContext.courier
        val bus = GameContext.bus

        world.interactables.find { it.id == "shelf" }?.action = {
            if (world.getHeld() != null) {
                bus.emit(Ev.NOTIFICATION, "Tangan masih membawa barang. Letakkan dulu di meja.")
            } else {
                val id = Catalog.products.firstOrNull { inv.stock(it.id) > 0 }?.id
                if (id == null) bus.emit(Ev.NOTIFICATION, "Stok habis! Beli di Smartphone → Supplier.")
                else {
                    inv.take(id)
                    world.setHeld(id)
                    GameContext.audio.play("box")
                    Settings.vibrate(25)
                    bus.emit(Ev.NOTIFICATION, "Mengambil ${Catalog.find(id)?.name}")
                }
            }
        }

        world.interactables.find { it.id == "table" }?.action = {
            val held = world.getHeld()
            when {
                held != null -> {
                    world.setHeld(null)
                    pack.begin(held)
                    bus.emit(Ev.NOTIFICATION, "Produk diletakkan di meja packing")
                }
                pack.readyPackage != null -> bus.emit(Ev.NOTIFICATION, "Paket sudah jadi. Serahkan di area pickup.")
                else -> {
                    val auto = GameContext.employees.takeAutoPicked()
                    if (auto != null) pack.begin(auto)
                    else bus.emit(Ev.NOTIFICATION, "Ambil produk dari rak dulu.")
                }
            }
        }

        world.interactables.find { it.id == "packing" }?.action = {
            if (pack.readyPackage != null) bus.emit(Ev.NOTIFICATION, "Paket selesai. Bawa ke area pickup.")
            else pack.interact()
        }

        world.interactables.find { it.id == "pickup" }?.action = {
            val id = pack.takePackage()
            if (id == null) {
                bus.emit(Ev.NOTIFICATION, "Belum ada paket siap. Packing dulu di meja.")
            } else {
                val order = ord.pendingFor(id)
                if (order == null) {
                    inv.returnStock(id)
                    bus.emit(Ev.NOTIFICATION, "Tidak ada pesanan untuk ${Catalog.find(id)?.name}. Barang kembali ke stok.")
                } else {
                    ord.complete(order)
                    GameContext.state.addCash(if (GameContext.upgrades.has("van")) 60_000L else 50_000L)
                    courier.call()
                }
            }
        }

        world.interactables.find { it.id == "computer" }?.action = { computer.toggle() }

        world.interactables.find { it.id == "door" }?.action = {
            bus.emit(Ev.NOTIFICATION, "Area luar & ekspansi gudang menyusul di fase berikutnya.")
        }
    }

    private fun startNew() {
        GameContext.state.reset()
        GameContext.upgrades.owned.clear()
        GameContext.employees.deserialize("")
        GameContext.state.inventory["kopi"] = 12
        GameContext.state.inventory["buku"] = 8
        GameContext.state.inventory["teh"] = 6
        world.setHeld(null)
        menu.root.isVisible = false
        menuVisible = false
        running = true
        GameContext.bus.emit(Ev.NOTIFICATION, "Selamat datang! Ambil produk dari rak, packing, lalu serahkan ke kurir.")
    }

    private fun startContinue() {
        if (!GameContext.save.load("auto")) {
            GameContext.bus.emit(Ev.NOTIFICATION, "Belum ada save. Mulai NEW GAME dulu.")
            return
        }
        menu.root.isVisible = false
        menuVisible = false
        running = true
        GameContext.bus.emit(Ev.NOTIFICATION, "Game dilanjutkan.")
    }

    private fun interact() {
        ray.set(camera.position.x, camera.position.y, camera.position.z, camera.direction.x, camera.direction.y, camera.direction.z)
        var best: Interactable? = null
        var bestT = Float.MAX_VALUE
        for (it in world.interactables) {
            if (!it.enabled) continue
            tmp.set(it.position).sub(ray.origin)
            val t = tmp.dot(ray.direction)
            if (t <= 0f || t > GameConfig.INTERACT_RANGE) continue
            closest.set(ray.direction).scl(t).add(ray.origin)
            if (closest.dst(it.position) <= it.radius && t < bestT) {
                bestT = t
                best = it
            }
        }
        currentTarget = best
        hud.setPrompt(if (best != null) "[E] ${best.label}" else "")
    }

    private fun performInteract() {
        val target = currentTarget
        Settings.vibrate(20)
        if (target != null) target.action()
        else GameContext.bus.emit(Ev.NOTIFICATION, "Arahkan kamera ke objek lalu tekan INTERAKSI")
    }

    private fun updateInput(dt: Float) {
        val mx = hud.touchpad.knobX
        val my = hud.touchpad.knobY
        var kbX = 0f
        var kbY = 0f
        if (Gdx.input.isKeyPressed(Input.Keys.A)) kbX -= 1f
        if (Gdx.input.isKeyPressed(Input.Keys.D)) kbX += 1f
        if (Gdx.input.isKeyPressed(Input.Keys.W)) kbY += 1f
        if (Gdx.input.isKeyPressed(Input.Keys.S)) kbY -= 1f

        moving = (mx != 0f || my != 0f || kbX != 0f || kbY != 0f)
        val jump = jumpQueued || Gdx.input.isKeyPressed(Input.Keys.SPACE)
        jumpQueued = false
        player.update(
            dt,
            mx + kbX,
            my + kbY,
            lookDx,
            lookDy,
            jump,
            Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT),
            Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
        )
        lookDx = 0f
        lookDy = 0f

        avatar.position.set(player.position.x, 0f, player.position.z)
        avatar.facing = player.facingDegrees()
        avatar.anim = when {
            celebrateTimer > 0f -> "CELEBRATE"
            world.getHeld() != null -> "CARRY"
            GameContext.packing.step >= 0 -> "PACK"
            moving -> if (player.sprinting) "RUN" else "WALK"
            else -> "IDLE"
        }
        avatar.update(dt, moving, player.sprinting)
        if (celebrateTimer > 0f) celebrateTimer -= dt

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) jumpQueued = true
    }

    override fun render(delta: Float) {
        val dt = minOf(delta, 0.05f)
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
        Gdx.gl.glClearColor(GameContext.time.sky.r, GameContext.time.sky.g, GameContext.time.sky.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        if (running) {
            updateInput(dt)
            GameContext.time.update(dt)
            GameContext.events.update(dt)
            GameContext.orders.update(dt)
            GameContext.courier.update(dt)
            GameContext.employees.update(dt)
            warehouseFX.update(dt)
            visitorTimer -= dt
            if (visitorTimer <= 0f) {
                visitor.spawn()
                visitorTimer = 75f + kotlin.random.Random.nextFloat() * 60f
            }
            visitor.update(dt)
            GameContext.save.update(dt)
            interact()
            world.update(dt, camera, player)
            world.updateLighting(GameContext.time.sunX, GameContext.time.sunY)
            weather.update(dt, camera, GameContext.time.rain)
            if (moving) {
                stepTimer -= dt
                if (stepTimer <= 0f) {
                    GameContext.audio.play("step", if (player.sprinting) .8f else .5f)
                    stepTimer = if (player.sprinting) .26f else .4f
                }
            }
        } else {
            camera.update()
            world.update(0f, camera, player)
        }

        batch.begin(camera)
        world.render(batch)
        warehouseFX.render(batch)
        GameContext.employees.render(batch)
        visitor.render(batch)
        if (player.thirdPerson) avatar.render(batch)
        batch.end()
        weather.render(camera, GameContext.time.rain)

        hud.update(dt)
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)
        stage.act(dt)
        stage.draw()
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
        phone.window.setPosition((stage.width - phone.window.width) / 2, 120f)
        computer.window.setPosition((stage.width - computer.window.width) / 2, 90f)
    }

    override fun dispose() {
        Settings.save()
        GameContext.audio.dispose()
        weather.dispose()
        warehouseFX.dispose()
        avatar.dispose()
        visitor.dispose()
        batch.dispose()
        world.dispose()
        stage.dispose()
        skin.dispose()
        UIStyles.dispose()
    }
}
