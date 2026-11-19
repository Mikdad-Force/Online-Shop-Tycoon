package com.ost.game.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.Align
import com.ost.game.core.Ev
import com.ost.game.core.GameContext
import com.ost.game.data.Catalog

/** HUD: kas, waktu, reputasi, order, notifikasi, tombol aksi & kontrol sentuh. */
class HUD(
    val stage: Stage,
    private val skin: com.badlogic.gdx.scenes.scene2d.ui.Skin,
    private val onAction: () -> Unit,
    private val onPhone: () -> Unit,
    private val onComputer: () -> Unit,
    private val onSave: () -> Unit,
    private val onView: () -> Unit
) {
    private val cashLabel = Label("", skin, "default").apply { setFontScale(1.1f) }
    private val timeLabel = Label("", skin, "default")
    private val repLabel = Label("", skin, "default")
    private val stageLabel = Label("", skin, "default")
    private val orderLabel = Label("", skin, "default").apply { setAlignment(Align.topLeft) }
    private val promptLabel = Label("", skin, "default").apply { setAlignment(Align.center) }
    private val eventLabel = Label("", skin, "default").apply { setAlignment(Align.center); setFontScale(1.15f) }
    private val fpsLabel = Label("", skin, "default")
    private val toastLabel = Label("", skin, "default").apply { setAlignment(Align.center); wrap = true }
    val touchpad = Touchpad(12f, skin)

    private var toastTimer = 0f

    init {
        val top = Table().apply { background = UIStyles.colorDrawable(0xCC102A34.toInt()) }
        top.add(stageLabel).padLeft(14f).padRight(24f)
        top.add(cashLabel).padRight(24f)
        top.add(timeLabel).padRight(24f)
        top.add(repLabel).padRight(24f)
        top.add(fpsLabel)
        top.row()

        val orders = Table().apply { background = UIStyles.colorDrawable(0x99102834.toInt()) }
        orders.add(Label("PESANAN MASUK", skin, "default")).left().padLeft(10f)
        orders.row()
        orders.add(orderLabel).left().padLeft(10f).padBottom(8f)

        val phoneBtn = btn("HP", onPhone)
        val pcBtn = btn("PC", onComputer)
        val saveBtn = btn("SAVE", onSave)
        val viewBtn = btn("VIEW", onView)
        val actionBtn = btn("INTERAKSI", onAction, large = true)

        val bottom = Table()
        bottom.add(touchpad).size(130f, 130f).pad(12f)
        bottom.add().expandX()
        bottom.add(phoneBtn).pad(6f)
        bottom.add(pcBtn).pad(6f)
        bottom.add(saveBtn).pad(6f)
        bottom.add(viewBtn).pad(6f)
        bottom.add(actionBtn).pad(12f)

        val center = Table()
        center.add(eventLabel).expandX().center().bottom().padBottom(168f).row()
        center.add(promptLabel).expandX().center().bottom().padBottom(120f)

        stage.addActor(Table().apply {
            setFillParent(true)
            touchable = com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly
            add(top).left().top().expandX().row()
            add(orders).left().top().padLeft(10f).padTop(6f).expandY().row()
            add(center).expandX().row()
            add(bottom).expandX().bottom()
        })

        stage.addActor(Table().apply {
            setFillParent(true)
            touchable = com.badlogic.gdx.scenes.scene2d.Touchable.disabled
            add(toastLabel).expandX().center().top().padTop(90f)
        })

        val bus = GameContext.bus
        bus.on(Ev.CASH_CHANGED) { cashLabel.setText("Rp ${String.format("%,d", it as Long)}") }
        bus.on(Ev.REP_CHANGED) { repLabel.setText("Reputasi: $it/100") }
        bus.on(Ev.DAY_CHANGED) { /* warna di-update lewat clock */ }
        bus.on(Ev.NOTIFICATION) { toast(it as String) }
        bus.on(Ev.LIVE_EVENT) { e ->
            val info = e as com.ost.game.systems.LiveEventInfo
            eventLabel.setText(if (info.name.isEmpty()) "" else "${info.name}  •  ${info.remaining.toInt()}d")
            eventLabel.color = if (info.name.isEmpty()) Color.WHITE else Color(1f, .85f, .35f, 1f)
        }

        cashLabel.setText("Rp ${String.format("%,d", GameContext.state.cash)}")
        repLabel.setText("Reputasi: ${GameContext.state.reputation}/100")
    }

    private fun btn(text: String, action: () -> Unit, large: Boolean = false): TextButton {
        return object : TextButton(text, skin) {
            override fun getPrefWidth(): Float = if (large) 190f else 78f
        }.apply {
            pad(if (large) 14f else 8f)
            addListener(object : com.badlogic.gdx.scenes.scene2d.InputListener() {
                override fun touchUp(event: com.badlogic.gdx.scenes.scene2d.InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    super.touchUp(event, x, y, pointer, button)
                    action()
                }
            })
        }
    }

    fun toast(msg: String) {
        toastLabel.setText(msg)
        toastLabel.color = Color.WHITE
        toastLabel.clearActions()
        toastLabel.addAction(Actions.sequence(Actions.alpha(1f), Actions.delay(2.6f), Actions.fadeOut(.5f)))
        toastTimer = 3f
    }

    fun setPrompt(text: String) { promptLabel.setText(text) }

    fun update(dt: Float) {
        val s = GameContext.state
        timeLabel.setText("Hari ${s.day} • ${GameContext.time.clockText()} • ${GameContext.time.phaseName()}")
        stageLabel.setText("${Catalog.stageName(s.stage)}")
        fpsLabel.setText("${Gdx.graphics.framesPerSecond} FPS")
        val sb = StringBuilder()
        s.orders.take(5).forEachIndexed { i, o ->
            val p = Catalog.find(o.productId)
            val t = (o.remaining / 60f).toInt()
            sb.append("${i + 1}. ${o.qty}x ${p?.name ?: o.productId} • ${t}m\n")
        }
        if (sb.isEmpty()) sb.append("Belum ada pesanan...")
        orderLabel.setText(sb.toString())
        if (toastTimer > 0f) toastTimer -= dt
    }
}
