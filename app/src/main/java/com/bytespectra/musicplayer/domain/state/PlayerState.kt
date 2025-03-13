package com.bytespectra.musicplayer.domain.state

import android.os.Parcelable
import com.bytespectra.musicplayer.domain.enum.PlayerAction
import com.bytespectra.musicplayer.domain.model.MusicData
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerState(
    val id: Long? = null,
    val music: MusicData? = null,
    var progress: Long? = null,
    var action: PlayerAction = PlayerAction.START
): Parcelable
