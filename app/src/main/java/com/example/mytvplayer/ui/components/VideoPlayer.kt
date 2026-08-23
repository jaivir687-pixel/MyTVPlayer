@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class, androidx.media3.common.util.UnstableApi::class)
package com.example.mytvplayer.ui.components

import android.view.KeyEvent
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.tv.material3.*
import com.example.mytvplayer.ui.theme.EpisodeTileBackground
import com.example.mytvplayer.ui.theme.RatingYellow
import com.example.mytvplayer.ui.theme.SidebarDark
import kotlinx.coroutines.delay

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var showControls by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var playbackProgress by remember { mutableStateOf(0f) }
    var currentPosLabel by remember { mutableStateOf("00:00") }
    var totalPosLabel by remember { mutableStateOf("00:00") }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val playerButtonColors = ButtonDefaults.colors(
        containerColor = EpisodeTileBackground.copy(alpha = 0.7f),
        contentColor = Color.White,
        focusedContainerColor = RatingYellow,
        focusedContentColor = Color.Black
    )

    val playButtonFocus = remember { FocusRequester() }
    val seekBarFocus = remember { FocusRequester() }
    val menuFirstButtonFocus = remember { FocusRequester() }

    var tracks by remember { mutableStateOf<Tracks?>(null) }

    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    totalPosLabel = formatTime(exoPlayer.duration)
                }
            }
            override fun onTracksChanged(newTracks: Tracks) {
                tracks = newTracks
            }
            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("VideoPlayer", "ExoPlayer Error: ${error.message}", error)
            }
        })
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPosLabel = formatTime(exoPlayer.currentPosition)
            if (exoPlayer.duration > 0) {
                playbackProgress = (exoPlayer.currentPosition.toFloat() / exoPlayer.duration).coerceIn(0f, 1f)
            }
            delay(1000)
        }
    }

    LaunchedEffect(showControls, isPlaying, showSettings, lastInteractionTime) {
        if (showControls && isPlaying && !showSettings) {
            delay(8000)
            showControls = false
        }
    }

    val parentFocusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(parentFocusRequester)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    lastInteractionTime = System.currentTimeMillis()
                    if (!showControls) {
                        showControls = true
                        return@onKeyEvent true
                    }
                }
                false
            }
            .focusable()
    ) {
        // ⭐ VIDEO VIEW ⭐
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                TextureView(ctx).apply {
                    keepScreenOn = true
                    // TV पर TextureView को focusable नहीं होना चाहिए ताकि यह key events न चुराए
                    isFocusable = false
                    isFocusableInTouchMode = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    exoPlayer.setVideoTextureView(this)
                }
            },
            update = { textureView ->
                exoPlayer.setVideoTextureView(textureView)
            }
        )

        // CONTROLLER OVERLAY
        AnimatedVisibility(
            visible = showControls || !isPlaying,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Button(onClick = { showSettings = true }, colors = playerButtonColors) {
                        Text("⚙ VIP Settings")
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier.focusRequester(playButtonFocus),
                        colors = playerButtonColors
                    ) {
                        Text(if (isPlaying) "PAUSE" else "PLAY")
                    }
                }

                var isSeekBarFocused by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(currentPosLabel, color = Color.White)
                        Text(totalPosLabel, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        onClick = { /* Activate Seek */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .focusRequester(seekBarFocus)
                            .onFocusChanged { isSeekBarFocused = it.isFocused }
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.nativeKeyEvent.keyCode) {
                                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                                            exoPlayer.seekTo((exoPlayer.currentPosition - 15000).coerceAtLeast(0))
                                            return@onKeyEvent true
                                        }
                                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                            exoPlayer.seekTo((exoPlayer.currentPosition + 15000).coerceAtMost(exoPlayer.duration))
                                            return@onKeyEvent true
                                        }
                                    }
                                }
                                false
                            },
                        shape = ClickableSurfaceDefaults.shape(RectangleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.Transparent,
                            focusedContainerColor = RatingYellow.copy(alpha = 0.1f)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isSeekBarFocused) 10.dp else 6.dp)
                                    .background(EpisodeTileBackground)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(playbackProgress)
                                        .fillMaxHeight()
                                        .background(if (isSeekBarFocused) RatingYellow else Color.White.copy(alpha = 0.7f))
                                )
                            }
                        }
                    }
                    if (isSeekBarFocused) {
                        Text(
                            "Use Left/Right to Seek",
                            style = MaterialTheme.typography.labelSmall,
                            color = RatingYellow
                        )
                    }
                }
            }
            LaunchedEffect(showControls) {
                if (showControls) {
                    playButtonFocus.requestFocus()
                } else {
                    parentFocusRequester.requestFocus()
                }
            }
        }

        if (showSettings) {
            BackHandler { showSettings = false }
            LaunchedEffect(Unit) { menuFirstButtonFocus.requestFocus() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.8f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .background(SidebarDark)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("VIP Control Panel", style = MaterialTheme.typography.headlineSmall, color = RatingYellow)

                        var captionsOn by remember {
                            mutableStateOf(!exoPlayer.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT))
                        }
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(menuFirstButtonFocus),
                            onClick = {
                                captionsOn = !captionsOn
                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !captionsOn).build()
                            },
                            colors = playerButtonColors
                        ) { Text(if (captionsOn) "Subtitles: ON" else "Subtitles: OFF") }

                        Text("Audio Tracks:", color = Color.Gray)
                        val audioGroups = tracks?.groups?.filter { it.type == C.TRACK_TYPE_AUDIO } ?: emptyList()
                        audioGroups.forEach { group ->
                            for (i in 0 until group.length) {
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i)).build()
                                    },
                                    colors = playerButtonColors
                                ) { Text(group.getTrackFormat(i).language?.uppercase() ?: "Audio Track ${i+1}") }
                            }
                        }

                        Text("Video Quality:", color = Color.Gray)
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                    .clearOverridesOfType(C.TRACK_TYPE_VIDEO).build()
                                showSettings = false
                            },
                            colors = playerButtonColors
                        ) { Text("Auto (Recommended)") }

                        val videoGroups = tracks?.groups?.filter { it.type == C.TRACK_TYPE_VIDEO } ?: emptyList()
                        videoGroups.forEach { group ->
                            for (i in 0 until group.length) {
                                if (group.isTrackSupported(i)) {
                                    val f = group.getTrackFormat(i)
                                    if (f.height > 0) {
                                        Button(
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = {
                                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i)).build()
                                                showSettings = false
                                            },
                                            colors = playerButtonColors
                                        ) { Text("${f.height}p") }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { showSettings = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = playerButtonColors
                        ) { Text("Back") }
                    }
                }
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.clearVideoTextureView(null)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "00:00"
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}