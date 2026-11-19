package com.ost.game

import com.badlogic.gdx.Game
import com.ost.game.screens.GameScreen

/** Entry point lintas-platform (libGDX Game). */
class OSTGame : Game() {
    override fun create() {
        setScreen(GameScreen())
    }
}
