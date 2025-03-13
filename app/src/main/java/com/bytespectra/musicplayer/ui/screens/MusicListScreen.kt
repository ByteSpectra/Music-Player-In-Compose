package com.bytespectra.musicplayer.ui.screens

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytespectra.musicplayer.R
import com.bytespectra.musicplayer.domain.enum.PlayerAction
import com.bytespectra.musicplayer.domain.event.MusicEvent
import com.bytespectra.musicplayer.domain.state.MusicState
import com.bytespectra.musicplayer.domain.util.Util
import com.bytespectra.musicplayer.ui.screens.viewModels.MusicViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MusicListScreen(
    modifier: Modifier = Modifier,
    musicViewModel: MusicViewModel,
    onSelectedAudio: (musicState: MusicEvent) -> Unit
) {
    val musicList by musicViewModel.musicState.collectAsStateWithLifecycle()

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    val mediaPermissionState = rememberPermissionState(permission = permission) {}
    val notificationPermissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS){}

    LaunchedEffect(true) {
        if (!notificationPermissionState.status.isGranted) {
            notificationPermissionState.launchPermissionRequest()
        }
    }

    if (mediaPermissionState.status.isGranted) {
        LaunchedEffect(key1 = true) {
            musicViewModel.loadFile()
        }

        MusicList(
            musicState = musicList,
            musicViewModel = musicViewModel,
            onSelectedAudio = onSelectedAudio
        )

    } else {
        LaunchPermission(permissionState = mediaPermissionState)
    }
}

@Composable
fun MusicList(
    modifier: Modifier = Modifier,
    musicState: MusicState,
    musicViewModel: MusicViewModel,
    onSelectedAudio: (musicState: MusicEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(color = Color.Black)
    ) {
        Text(
            text = "My Music",
            fontSize = 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(musicState.musicList.size) { index ->
                val music = musicState.musicList[index]
                Card(
                    onClick = {
                        onSelectedAudio.invoke(MusicEvent.Start(music))
                    },
                    colors = CardColors(
                        contentColor = Color.White,
                        containerColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(80.dp).padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = Util.truncateName(music.name ?: ""))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = Util.calculateDuration(music.duration ?: 0))
                        }
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        musicState.playerState?.let { player ->
            player.music?.let { selectedMusic ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .padding(10.dp)
                        .background(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = Util.truncateName(selectedMusic.name ?: ""),
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        ) {
                            Slider(
                                value = musicState.sliderState,
                                onValueChange = {
                                    onSelectedAudio(MusicEvent.SliderChange(it, selectedMusic))
                                },
                                onValueChangeFinished = {

                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Gray.copy(),
                                    activeTrackColor = Color.Gray.copy(),
                                    activeTickColor = Color.LightGray.copy()
                                ),
                                valueRange = 0f..(selectedMusic.duration ?: 0L).toFloat()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = Util.calculateDuration(musicState.sliderState.toLong()),
                                    modifier = Modifier,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = Util.calculateDuration(selectedMusic.duration ?: 0L),
                                    modifier = Modifier,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Color.Transparent)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_previous),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        musicViewModel.onAction(MusicEvent.Previous(selectedMusic))
                                        onSelectedAudio.invoke(MusicEvent.Previous(selectedMusic))
                                    }
                            )
                            val actionIcon = when (player.action == PlayerAction.PAUSE) {
                                true -> painterResource(R.drawable.ic_play)
                                false -> painterResource(R.drawable.ic_pause)
                            }
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(
                                        color = Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        when (player.action == PlayerAction.PAUSE) {
                                            true -> onSelectedAudio(MusicEvent.Play(selectedMusic))
                                            false -> onSelectedAudio(MusicEvent.Pause(selectedMusic))
                                        }
                                    }
                            ) {
                                Icon(
                                    painter = actionIcon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            Icon(
                                painter = painterResource(R.drawable.ic_next),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onSelectedAudio(MusicEvent.Next(selectedMusic))
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LaunchPermission(
    modifier: Modifier = Modifier,
    permissionState: PermissionState,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "The permission is needed to process the application")
        Button(onClick = {
            permissionState.launchPermissionRequest()
        }) {
            Text(text = "Request Permission")
        }
    }
}