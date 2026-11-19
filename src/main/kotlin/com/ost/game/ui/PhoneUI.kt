package com.ost.game.ui

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.ost.game.core.GameContext
import com.ost.game.data.Catalog

/** Smartphone in-game: Marketplace, Supplier, Bank, Chat. */
class PhoneUI(private val skin: Skin) {
    val window = Window("SMARTPHONE", skin)
    private val content = Table()
    private var tab = "MARKETPLACE"

    init {
        window.setSize(520f, 560f)
        window.isMovable = true
        window.isModal = false
        val tabs = Table()
        listOf("MARKETPLACE", "SUPPLIER", "BANK", "CHAT", "SETTING").forEach { t ->
            tabs.add(object : TextButton(t, skin) {
                override fun getPrefWidth(): Float = 100f
            }.apply {
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        tab = t; refresh()
                    }
                })
            }).pad(4f)
        }
        window.add(tabs).expandX().row()
        window.add(ScrollPane(content, skin)).expand().fill().row()
        window.add(object : TextButton("TUTUP", skin) {
            override fun getPrefWidth(): Float = 520f
        }.apply { addListener(object : ClickListener() { override fun clicked(e: InputEvent?, x: Float, y: Float) { hide() } }) })
        window.isVisible = false
        refresh()
    }

    fun toggle() { window.isVisible = !window.isVisible; if (window.isVisible) refresh() }
    fun hide() { window.isVisible = false }

    fun refresh() {
        content.clear()
        val s = GameContext.state
        when (tab) {
            "MARKETPLACE" -> {
                content.add(Label("Produk aktif di marketplace", skin)).left().pad(8f).row()
                Catalog.products.forEach { p ->
                    val row = Table()
                    row.add(Label("${p.name} [${p.sku}]", skin)).width(210f).padLeft(8f)
                    row.add(Label("Stok ${GameContext.inventory.stock(p.id)}", skin)).width(100f)
                    row.add(Label("Jual Rp ${String.format("%,d", p.sellPrice)}", skin)).width(140f)
                    content.add(row).expandX().fillX().row()
                }
            }
            "SUPPLIER" -> {
                content.add(Label("Beli stok grosir (harga beli)", skin)).left().pad(8f).row()
                Catalog.products.forEach { p ->
                    val row = Table()
                    row.add(Label(p.name, skin)).width(180f).padLeft(8f)
                    row.add(Label("Rp ${String.format("%,d", p.buyPrice)}", skin)).width(120f)
                    row.add(buyBtn(p.id, 1, "x1")).pad(2f)
                    row.add(buyBtn(p.id, 10, "x10")).pad(2f)
                    content.add(row).expandX().fillX().row()
                }
            }
            "BANK" -> {
                content.add(Label("Kas: Rp ${String.format("%,d", s.cash)}", skin)).pad(10f).row()
                content.add(Label("Pinjaman: Rp ${String.format("%,d", s.loan)}", skin)).pad(4f).row()
                content.add(object : TextButton("Pinjam Rp 1.000.000", skin) {
                    override fun getPrefWidth(): Float = 300f
                }.apply { addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        s.addCash(1_000_000); s.loan += 1_000_000; refresh()
                    }
                }) }).pad(8f).row()
                content.add(object : TextButton("Lunasi pinjaman", skin) {
                    override fun getPrefWidth(): Float = 300f
                }.apply { addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        if (s.loan > 0 && s.spend(s.loan)) s.loan = 0; refresh()
                    }
                }) }).pad(8f).row()
            }
            "CHAT" -> {
                content.add(Label("Pembeli: Kak, barangnya ready?", skin)).left().pad(8f).row()
                content.add(Label("Anda: Ready kak, langsung dikirim hari ini 😊", skin)).left().padLeft(30f).row()
                content.add(Label("Supplier: Promo bulk hari ini, beli 10 gratis 1!", skin)).left().pad(8f).row()
            }
            "SETTING" -> {
                val st = com.ost.game.core.Settings
                content.add(Label("MUSIK: ${if (st.musicOn) "ON" else "OFF"}", skin)).pad(6f).row()
                content.add(toggleBtn("MUSIK") { st.musicOn = !st.musicOn }).pad(4f).row()
                content.add(Label("MUSIK: ${(st.musicVolume * 100).toInt()}%  |  SFX: ${(st.sfxVolume * 100).toInt()}%", skin)).pad(6f).row()
                content.add(adjustBtn("MUSIK -") { st.musicVolume = (st.musicVolume - .1f).coerceIn(0f, 1f) }).pad(2f)
                content.add(adjustBtn("MUSIK +") { st.musicVolume = (st.musicVolume + .1f).coerceIn(0f, 1f) }).pad(2f).row()
                content.add(adjustBtn("SFX -") { st.sfxVolume = (st.sfxVolume - .1f).coerceIn(0f, 1f) }).pad(2f)
                content.add(adjustBtn("SFX +") { st.sfxVolume = (st.sfxVolume + .1f).coerceIn(0f, 1f) }).pad(2f).row()
                content.add(Label("HAPTIC: ${if (st.haptics) "ON" else "OFF"}  |  SENSITIVITAS: ${String.format("%.1f", st.sensitivity)}", skin)).pad(6f).row()
                content.add(toggleBtn("HAPTIC") { st.haptics = !st.haptics }).pad(2f)
                content.add(adjustBtn("SENS -") { st.sensitivity = (st.sensitivity - .1f).coerceIn(.3f, 2.5f) }).pad(2f)
                content.add(adjustBtn("SENS +") { st.sensitivity = (st.sensitivity + .1f).coerceIn(.3f, 2.5f) }).pad(2f).row()
                content.add(object : TextButton("SIMPAN PENGATURAN", skin) {
                    override fun getPrefWidth(): Float = 320f
                }.apply { addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        st.save()
                        GameContext.bus.emit(com.ost.game.core.Ev.SETTINGS_CHANGED)
                        st.vibrate(40)
                        GameContext.bus.emit(com.ost.game.core.Ev.NOTIFICATION, "Pengaturan tersimpan")
                    }
                }) }).pad(10f).row()
            }
        }
    }

    private fun toggleBtn(text: String, action: () -> Unit): TextButton =
        object : TextButton(text, skin) {
            override fun getPrefWidth(): Float = 150f
            override fun getPrefHeight(): Float = 44f
        }.apply { addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) { action(); refresh() }
        }) }

    private fun adjustBtn(text: String, action: () -> Unit): TextButton =
        object : TextButton(text, skin) {
            override fun getPrefWidth(): Float = 105f
            override fun getPrefHeight(): Float = 44f
        }.apply { addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) { action(); refresh() }
        }) }

    private fun buyBtn(id: String, qty: Int, text: String): TextButton {
        return object : TextButton(text, skin) {
            override fun getPrefWidth(): Float = 70f
            override fun getPrefHeight(): Float = 40f
        }.apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    GameContext.inventory.buy(id, qty); refresh()
                }
            })
        }
    }
}
