package com.ost.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

object UIStyles {
    val textures = mutableListOf<Texture>()

    fun colorDrawable(color: Int): TextureRegionDrawable {
        val pm = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pm.setColor(Color(color))
        pm.fill()
        val tex = Texture(pm)
        pm.dispose()
        textures.add(tex)
        return TextureRegionDrawable(TextureRegion(tex))
    }

    fun createSkin(): Skin {
        val skin = Skin()
        val font = BitmapFont()
        skin.add("default", font)
        skin.add("default", colorDrawable(0x00000000))
        skin.add("default", Label.LabelStyle(font, Color.WHITE))
        skin.add("default", TextButton.TextButtonStyle(
            colorDrawable(0x2E8B8B), colorDrawable(0x1F6E6E), colorDrawable(0x245B5B), font
        ).apply {
            fontColor = Color.WHITE
            downFontColor = Color(0.8f, 0.95f, 0.95f, 1f)
        })
        skin.add("default", Window.WindowStyle(font, Color.WHITE, colorDrawable(0xEE263238.toInt())))
        skin.add("default", Touchpad.TouchpadStyle(colorDrawable(0x552E8B8B), colorDrawable(0x992E8B8B.toInt())))
        skin.add("default", ScrollPane.ScrollPaneStyle())
        return skin
    }

    fun dispose() {
        textures.forEach { it.dispose() }
        textures.clear()
    }
}
