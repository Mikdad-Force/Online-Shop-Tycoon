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
import com.ost.game.core.GameConfig
import com.ost.game.data.Catalog

/** Komputer: Dashboard order, inventory, keuangan, karyawan, marketing. */
class ComputerUI(private val skin: Skin) {
    val window = Window("KOMPUTER - DASHBOARD", skin)
    private val content = Table()
    private var tab = "ORDER"

    init {
        window.setSize(640f, 600f)
        window.isMovable = true
        val tabs = Table()
        listOf("ORDER", "INVENTORY", "FINANCE", "STAFF", "UPGRADE", "MARKETING").forEach { t ->
            tabs.add(object : TextButton(t, skin) { override fun getPrefWidth(): Float = 100f }.apply {
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) { tab = t; refresh() }
                })
            }).pad(3f)
        }
        window.add(tabs).expandX().row()
        window.add(ScrollPane(content, skin)).expand().fill().row()
        window.add(object : TextButton("TUTUP", skin) { override fun getPrefWidth(): Float = 640f }.apply {
            addListener(object : ClickListener() { override fun clicked(e: InputEvent?, x: Float, y: Float) { hide() } })
        })
        window.isVisible = false
        refresh()
    }

    fun toggle() { window.isVisible = !window.isVisible; if (window.isVisible) refresh() }
    fun hide() { window.isVisible = false }

    fun refresh() {
        content.clear()
        val s = GameContext.state
        when (tab) {
            "ORDER" -> {
                content.add(Label("Antrian pesanan (${s.orders.size})", skin)).left().pad(8f).row()
                if (s.orders.isEmpty()) content.add(Label("Belum ada pesanan masuk.", skin)).pad(8f).row()
                s.orders.toList().forEach { o ->
                    val p = Catalog.find(o.productId)
                    val row = Table()
                    row.add(Label("#${o.id}  ${o.qty}x ${p?.name ?: o.productId}", skin)).width(260f).padLeft(8f)
                    row.add(Label("${(o.remaining / 60f).toInt()} menit", skin)).width(110f)
                    row.add(Label("Rp ${String.format("%,d", o.reward)}", skin)).width(130f)
                    row.add(object : TextButton("Batalkan", skin) { override fun getPrefWidth(): Float = 90f }.apply {
                        addListener(object : ClickListener() {
                            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                s.orders.remove(o); s.addReputation(-3); refresh()
                            }
                        })
                    })
                    content.add(row).expandX().fillX().row()
                }
            }
            "INVENTORY" -> {
                content.add(Label("Stok gudang ${GameContext.inventory.totalItems()}/${s.storageCapacity} item", skin)).left().pad(8f).row()
                Catalog.products.forEach { p ->
                    val row = Table()
                    row.add(Label("${p.name} [${p.sku}]", skin)).width(230f).padLeft(8f)
                    row.add(Label("${GameContext.inventory.stock(p.id)} pcs", skin))
                    content.add(row).expandX().fillX().row()
                }
            }
            "FINANCE" -> {
                val income = s.orders.sumOf { it.reward }
                content.add(Label("Kas saat ini: Rp ${String.format("%,d", s.cash)}", skin)).pad(8f).row()
                content.add(Label("Order aktif (potensi): Rp ${String.format("%,d", income)}", skin)).pad(4f).row()
                content.add(Label("Pesanan selesai: ${s.ordersDone}", skin)).pad(4f).row()
                content.add(Label("Produk terjual: ${s.productsSold}", skin)).pad(4f).row()
                content.add(Label("Pinjaman: Rp ${String.format("%,d", s.loan)}", skin)).pad(4f).row()
                val daily = GameConfig.DAILY_RENT + GameConfig.DAILY_SALARY + s.employeeSalary
                val tax = (s.cash * 0.02f).toLong()
                content.add(Label("Gaji karyawan/hari: Rp ${String.format("%,d", s.employeeSalary)}", skin)).pad(4f).row()
                content.add(Label("Total biaya harian: Rp ${String.format("%,d", daily)}", skin)).pad(4f).row()
                content.add(Label("Estimasi pajak (2% kas): Rp ${String.format("%,d", tax)}", skin)).pad(4f).row()
                content.add(Label("Stage: ${s.stage} — ${com.ost.game.data.Catalog.stageName(s.stage)}", skin)).pad(4f).row()
            }
            "STAFF" -> {
                content.add(Label("Karyawan aktif: ${GameContext.employees.employees.size}", skin)).left().pad(8f).row()
                com.ost.game.systems.Role.entries.forEach { role ->
                    val hired = GameContext.employees.employees.any { it.role == role }
                    val row = Table()
                    row.add(Label("${role.label} — Rp ${String.format("%,d", role.hireCost)}", skin)).width(280f).padLeft(8f)
                    row.add(object : TextButton(if (hired) "BERHENTIKAN" else "REKRUT", skin) {
                        override fun getPrefWidth(): Float = 150f
                        override fun getPrefHeight(): Float = 44f
                    }.apply { addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            if (hired) GameContext.employees.fire(role) else GameContext.employees.hire(role)
                            refresh()
                        }
                    }) })
                    content.add(row).expandX().fillX().row()
                }
                content.add(Label("Picker otomatis menyiapkan barang di meja packing.", skin)).left().padLeft(8f).padTop(10f).row()
                content.add(Label("Packer otomatis memajukan tahap packing bila ada paket.", skin)).left().padLeft(8f).row()
            }
            "UPGRADE" -> {
                content.add(Label("Upgrade fasilitas (kas: Rp ${String.format("%,d", s.cash)})", skin)).left().pad(8f).row()
                com.ost.game.systems.Upgrade.entries.forEach { u ->
                    val owned = GameContext.upgrades.has(u.id)
                    val row = Table()
                    row.add(Label("${u.title}\n${u.desc}", skin)).width(300f).padLeft(8f)
                    row.add(Label(if (owned) "DIMILIKI" else "Rp ${String.format("%,d", u.cost)}", skin)).width(130f)
                    if (!owned) row.add(object : TextButton("BELI", skin) {
                        override fun getPrefWidth(): Float = 90f
                        override fun getPrefHeight(): Float = 44f
                    }.apply { addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            GameContext.upgrades.buy(u); refresh()
                        }
                    }) })
                    content.add(row).expandX().fillX().row()
                }
            }
            "MARKETING" -> {
                content.add(Label("Reputasi: ${s.reputation}/100", skin)).pad(8f).row()
                content.add(object : TextButton("Pasang Iklan (Rp 250.000)", skin) {
                    override fun getPrefWidth(): Float = 320f
                }.apply { addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        if (s.spend(250_000)) { s.addReputation(4); GameContext.bus.emit(
                            com.ost.game.core.Ev.NOTIFICATION, "Iklan dipasang! Reputasi +4") }
                        refresh()
                    }
                }) }).pad(8f).row()
            }
        }
    }
}
