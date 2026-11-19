package com.ost.game.core

import com.badlogic.gdx.Gdx

/** Pengaturan pengguna, disimpan di penyimpanan internal aplikasi. */
object Settings {
    var musicOn = true
    var sfxVolume = 1f
    var musicVolume = 0.5f
    var haptics = true
    var sensitivity = 1f
    var quality = 1 // 0 rendah, 1 sedang, 2 tinggi

    fun load() {
        val f = Gdx.files.local("settings.txt")
        if (!f.exists()) return
        try {
            f.readString().lineSequence().forEach { line ->
                val kv = line.split("=", limit = 2)
                if (kv.size != 2) return@forEach
                when (kv[0]) {
                    "musicOn" -> musicOn = kv[1].toBoolean()
                    "sfxVolume" -> sfxVolume = kv[1].toFloat().coerceIn(0f, 1f)
                    "musicVolume" -> musicVolume = kv[1].toFloat().coerceIn(0f, 1f)
                    "haptics" -> haptics = kv[1].toBoolean()
                    "sensitivity" -> sensitivity = kv[1].toFloat().coerceIn(.3f, 2.5f)
                    "quality" -> quality = kv[1].toInt().coerceIn(0, 2)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun save() {
        val data = listOf(
            "musicOn=$musicOn", "sfxVolume=$sfxVolume", "musicVolume=$musicVolume",
            "haptics=$haptics", "sensitivity=$sensitivity", "quality=$quality"
        ).joinToString("\n")
        Gdx.files.local("settings.txt").writeString(data, false)
    }

    fun vibrate(ms: Int) {
        if (!haptics) return
        try { Gdx.input.vibrate(ms) } catch (_: Exception) { }
    }
}
