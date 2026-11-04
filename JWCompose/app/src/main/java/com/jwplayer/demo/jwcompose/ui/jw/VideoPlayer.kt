package com.jwplayer.demo.jwcompose.ui.jw

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.jwplayer.demo.jwcompose.R
import com.jwplayer.pub.api.JWPlayer

/** The player with a Compose control bar on top. Tap the video to show or hide the controls. */
@Composable
fun VideoPlayer(
  modifier: Modifier = Modifier,
  videoState: VideoState,
  onPlayerCreated: (JWPlayer) -> Unit,
  onPlayerDisposed: (JWPlayer) -> Unit,
  onTogglePlayback: () -> Unit
) {
  Box(modifier = modifier) {
    var controlsVisible by rememberSaveable { mutableStateOf(true) }

    val controlsClickLabel = if (controlsVisible) {
      R.string.label_hide_controls
    } else {
      R.string.label_display_controls
    }

    // Player view at the bottom layer
    Playback(
      modifier = Modifier
        .fillMaxSize()
        .clickable(onClickLabel = stringResource(id = controlsClickLabel)) {
          controlsVisible = !controlsVisible
        }
        .zIndex(0f),
      onPlayerCreated = onPlayerCreated,
      onPlayerDisposed = onPlayerDisposed
    )

    val alpha by animateFloatAsState(
      targetValue = if (controlsVisible) 0.7f else 0f,
      animationSpec = tween(delayMillis = if (controlsVisible) 0 else 750),
      label = "controlsAlpha"
    )

    // Controls overlay at the top layer
    Controls(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .alpha(alpha)
        .zIndex(1f),
      playerState = videoState.playerState,
      onTogglePlayback = onTogglePlayback
    )
  }
}
