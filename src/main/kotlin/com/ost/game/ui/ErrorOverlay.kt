package com.ost.game.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align

/** Menampilkan error runtime di layar (menggantikan "layar putih" silents). */
class ErrorOverlay(skin: Skin) {
    val root = Table().apply {
        setFillParent(true)
        background = UIStyles.colorDrawable(0xF0101B24.toInt())
        isVisible = false
    }
    private val label = Label("", skin).apply {
        setAlignment(Align.topLeft)
        wrap = true
        setFontScale(.85f)
    }

    init {
        root.add(label).expand().fill().pad(30f)
    }

    fun show(e: Throwable) {
        val sw = java.io.StringWriter()
        e.printStackTrace(java.io.PrintWriter(sw))
        val msg = sw.toString().take(3500)
        label.setText("TERJADI ERROR\n\n$msg")
        root.isVisible = true
        root.toFront()
        Gdx.app.error("OST", "Runtime error", e)
    }
}
