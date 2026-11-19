package com.ost.game.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.ost.game.core.Ev
import com.ost.game.core.EventBus
import com.ost.game.core.Settings

/** Audio system: SFX + BGM, terhubung ke EventBus (bukan dipanggil manual tiap tempat). */
class AudioSystem(private val bus: EventBus) {
    private val sounds = HashMap<String, Sound>()
    private var music: Music? = null

    fun load() {
        listOf("beep", "click", "tape", "box", "printer", "step", "coin",
            "error", "success", "truck", "rain").forEach { name ->
            try {
                sounds[name] = Gdx.audio.newSound(Gdx.files.internal("audio/$name.wav"))
            } catch (e: Exception) { e.printStackTrace() }
        }
        try {
            music = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm.wav"))
            music?.isLooping = true
            applyMusic()
        } catch (e: Exception) { e.printStackTrace() }
        wire()
    }

    private fun wire() {
        bus.on(Ev.ORDER_ADDED) { play("beep") }
        bus.on(Ev.ORDER_DONE) { play("coin", 1f); Settings.vibrate(60) }
        bus.on(Ev.ORDER_FAILED) { play("error") }
        bus.on(Ev.PACKING_STEP) { payload ->
            val s = (payload as? String) ?: ""
            play(if ("Bubble" in s || "Lakban" in s || "resi" in s) "tape" else "click")
            Settings.vibrate(18)
        }
        bus.on(Ev.PACKING_DONE) { play("success"); Settings.vibrate(90) }
        bus.on(Ev.COURIER) { payload ->
            if ((payload as? String)?.contains("menuju") == true) play("truck", .8f)
        }
        bus.on(Ev.SAVED) { play("click", .7f) }
        bus.on(Ev.SETTINGS_CHANGED) { applyMusic() }
    }

    fun play(name: String, volume: Float = 1f) {
        sounds[name]?.play(Settings.sfxVolume * volume)
    }

    fun startMusic() {
        if (Settings.musicOn) music?.play()
    }

    fun applyMusic() {
        music?.volume = Settings.musicVolume.coerceIn(0f, 1f)
        if (!Settings.musicOn) music?.stop()
        else music?.let { m -> if (!m.isPlaying) m.play() }
    }

    fun dispose() {
        sounds.values.forEach { it.dispose() }
        sounds.clear()
        music?.dispose()
    }
}
