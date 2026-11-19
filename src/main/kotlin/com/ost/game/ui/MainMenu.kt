package com.ost.game.ui

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

/** Main menu: New Game, Continue, petunjuk kontrol. */
class MainMenu(
    skin: Skin,
    private val onNew: () -> Unit,
    private val onContinue: () -> Unit
) {
    val root = Table()

    init {
        root.setFillParent(true)
        root.background = UIStyles.colorDrawable(0xEE0E1B24.toInt())
        val title = Label("ONLINE SHOP TYCOON", skin).apply { setFontScale(2.2f) }
        val sub = Label("3D Cartoon Business Simulator", skin)
        val newBtn = big(skin, "NEW GAME", onNew)
        val contBtn = big(skin, "LANJUTKAN", onContinue)
        val help = Label(
            "KONTROL: Joystick kiri = jalan • Geser layar = lihat • INTERAKSI = ambil/letak/packing\n" +
            "Tombol PC = dashboard  •  Tombol HP = marketplace & supplier  •  SAVE = simpan",
            skin
        ).apply { setFontScale(.9f) }
        root.add(title).center().padTop(60f).row()
        root.add(sub).center().padBottom(40f).row()
        root.add(newBtn).width(320f).height(64f).pad(8f).row()
        root.add(contBtn).width(320f).height(64f).pad(8f).row()
        root.add(help).center().padTop(40f).expandY()
    }

    private fun big(skin: Skin, text: String, action: () -> Unit): TextButton =
        object : TextButton(text, skin) {
            override fun getPrefHeight(): Float = 60f
        }.apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) = action()
            })
        }
}
