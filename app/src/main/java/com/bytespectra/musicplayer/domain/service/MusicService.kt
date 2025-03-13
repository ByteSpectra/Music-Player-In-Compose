package com.bytespectra.musicplayer.domain.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.bytespectra.musicplayer.MainActivity
import com.bytespectra.musicplayer.R
import com.bytespectra.musicplayer.domain.enum.PlayerAction
import com.bytespectra.musicplayer.domain.model.MusicData
import com.bytespectra.musicplayer.domain.state.PlayerState
import com.bytespectra.musicplayer.domain.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "music_player_channel"
    }

    private val mediaPlayer: MediaPlayer by lazy { MediaPlayer() }
    private var defaultCoroutineScope = CoroutineScope(Dispatchers.Default)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()

        registerReceiver(playerStateReceiver, IntentFilter(Util.PLAYER_STATE_CHANNEL), Context.RECEIVER_NOT_EXPORTED)
        registerReceiver(sliderChangeReceiver, IntentFilter(Util.PLAYER_STATE_CHANNEL), Context.RECEIVER_NOT_EXPORTED)

        return START_STICKY
    }

    private fun startNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playing Audio")
            .setContentText("Your audio is playing in the background")
            .setSmallIcon(R.drawable.ic_play)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    val playerStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                it.getParcelableExtra<PlayerState>(Util.PLAYER)?.let { play ->
                    onPlayerAction(play)
                }
            }
        }
    }

    val sliderChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                updateDuration(it.getFloatExtra(Util.SLIDER_CHANGE_VALUE, 0f))
            }
        }
    }

    private fun updateDuration(value: Float) {
        if (value != 0f) {
            mediaPlayer.seekTo(value.roundToInt())
        }
    }

    private fun onPlayerAction(state: PlayerState) {
        defaultCoroutineScope.launch {
            when (state.action) {
                PlayerAction.PLAY -> {
                    mediaPlayer.start()
                    sendBroadcast(Intent(Util.PROGRESS_CHANNEL).apply {
                        putExtra("action", "Play")
                        putExtra("currentPosition", mediaPlayer.currentPosition.toLong())
                        putExtra("duration", mediaPlayer.duration.toLong())
                    })
                }
                PlayerAction.PAUSE -> {
                    mediaPlayer.pause()
                    sendBroadcast(Intent(Util.PROGRESS_CHANNEL).apply {
                        putExtra("action", "Pause")
                        putExtra("currentPosition", mediaPlayer.currentPosition.toLong())
                        putExtra("duration", mediaPlayer.duration.toLong())
                    })
                }
                PlayerAction.NEXT,
                PlayerAction.START,
                PlayerAction.PREVIOUS -> { changeMediaSource(state.music) }
            }
        }
    }

    private fun changeMediaSource(music: MusicData?) {
        music?.let {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(it.filePath)
            mediaPlayer.isLooping = false
            mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
            mediaPlayer.prepareAsync()
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mediaPlayer.isPlaying) mediaPlayer.stop()
        mediaPlayer.release()
        stopSelf()
        defaultCoroutineScope.cancel()
        unregisterReceiver(playerStateReceiver)
        unregisterReceiver(sliderChangeReceiver )
    }
}