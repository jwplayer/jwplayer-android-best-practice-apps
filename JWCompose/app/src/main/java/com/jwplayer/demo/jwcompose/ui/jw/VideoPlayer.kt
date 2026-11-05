package com.jwplayer.demo.jwcompose.ui.jw

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
        .zIndex(0f),
      onPlayerCreated = onPlayerCreated,
      onPlayerDisposed = onPlayerDisposed
    )

    // Tap target above the player. A Modifier.clickable on the AndroidView itself is unreliable:
    // touches go to the embedded JWPlayerView first, and when it consumes them the click never
    // fires. A transparent Compose layer on top always receives the tap. The SDK's own controls
    // are hidden in this sample, so nothing underneath needs the touch. The Compose controls sit
    // above this layer only while they are visible.
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clickable(onClickLabel = stringResource(id = controlsClickLabel)) {
          controlsVisible = !controlsVisible
        }
        .zIndex(0.5f)
    )

    // Controls overlay at the top layer. AnimatedVisibility removes the controls from
    // composition once they have faded out, so hidden controls take no touches: a tap anywhere,
    // including the bottom strip where the bar sits, reaches the tap layer and shows them again.
    AnimatedVisibility(
      visible = controlsVisible,
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .zIndex(1f),
      enter = fadeIn(),
      exit = fadeOut(animationSpec = tween(delayMillis = 750))
    ) {
      Controls(
        modifier = Modifier
          .fillMaxWidth()
          .alpha(0.7f),
        playerState = videoState.playerState,
        onTogglePlayback = onTogglePlayback
      )
    }
  }
}
