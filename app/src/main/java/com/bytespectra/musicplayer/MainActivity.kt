package com.bytespectra.musicplayer

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.bytespectra.musicplayer.domain.event.MusicEvent
import com.bytespectra.musicplayer.domain.service.MusicService
import com.bytespectra.musicplayer.domain.util.Util
import com.bytespectra.musicplayer.ui.screens.MusicListScreen
import com.bytespectra.musicplayer.ui.screens.viewModels.MusicViewModel
import com.bytespectra.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : ComponentActivity() {

    private val musicViewModel by viewModels<MusicViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        startForegroundService(Intent(this, MusicService::class.java))

        setContent {
            MusicPlayerTheme {
                Scaffold(
                    modifier = Modifier.safeContentPadding().fillMaxSize()
                ) { innerPadding ->
                    MusicListScreen(
                        modifier = Modifier.padding(innerPadding),
                        musicViewModel = musicViewModel
                    ) { musicEvent ->
                        musicViewModel.onAction(musicEvent)
                        if (musicEvent is MusicEvent.SliderChange) {
                            (musicEvent as? MusicEvent.SliderChange)?.let {
                                sendBroadcast(Intent(Util.PLAYER_STATE_CHANNEL).apply {
                                    putExtra(Util.SLIDER_CHANGE_VALUE, it.duration)
                                })
                            }
                        } else {
                            sendBroadcast(Intent(Util.PLAYER_STATE_CHANNEL).apply {
                                putExtra("player", musicViewModel.musicState.value.playerState)
                            })
                        }
                    }
                }
            }
        }

        musicViewModel.setContentResolver(contentResolver)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        registerReceiver(
            musicViewModel.progressBroadcastReceiver,
            IntentFilter(Util.PROGRESS_CHANNEL),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(musicViewModel.progressBroadcastReceiver)
    }
}
