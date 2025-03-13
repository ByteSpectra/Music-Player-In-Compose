package com.bytespectra.musicplayer.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicData(
    val id: Long? = null,
    val name: String? = null,
    val duration: Long? = null,
    val filePath: String? = null
) : Parcelable
