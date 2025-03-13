package com.bytespectra.musicplayer.domain.event

import com.bytespectra.musicplayer.domain.model.MusicData

sealed interface MusicEvent {
    data class Start(val musicData: MusicData): MusicEvent
    data class Play(val musicData: MusicData): MusicEvent
    data class Pause(val musicData: MusicData): MusicEvent
    data class Next(val musicData: MusicData): MusicEvent
    data class Previous(val musicData: MusicData): MusicEvent
    data class SliderChange(val duration: Float, val musicData: MusicData): MusicEvent
}