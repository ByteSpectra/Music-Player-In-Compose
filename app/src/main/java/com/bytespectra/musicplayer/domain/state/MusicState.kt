package com.bytespectra.musicplayer.domain.state

import com.bytespectra.musicplayer.domain.model.MusicData

data class MusicState(
    val musicList: MutableList<MusicData> = mutableListOf(),
    val playerState: PlayerState? = null,
    val sliderState: Float = 0f
)
