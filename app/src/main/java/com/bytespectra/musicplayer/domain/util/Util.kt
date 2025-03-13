package com.bytespectra.musicplayer.domain.util

import java.util.Locale
import java.util.concurrent.TimeUnit

object Util {
    const val PLAYER = "player"
    const val SLIDER_CHANGE_VALUE = "slider_change_value"
    const val PLAYER_STATE_CHANNEL = "player_state_channel"
    const val SLIDER_STATE_CHANNEL = "slider_state_channel"
    const val PROGRESS_CHANNEL = "progress_channel"

    fun calculateDuration(milli: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milli)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milli) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milli) - TimeUnit.MINUTES.toSeconds(minutes)

        val formattedDuration = String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)
        return formattedDuration
    }

    fun truncateName(name: String): String {
        var truncateName = ""
        if (name.endsWith("mp3")) {
            truncateName = name.substring(startIndex = 0, endIndex = name.length - 4)
        }
        return truncateName
    }
}