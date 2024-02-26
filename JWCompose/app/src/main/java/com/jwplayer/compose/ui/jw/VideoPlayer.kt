package com.jwplayer.compose.ui.jw

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import com.jwplayer.compose.R
import com.jwplayer.compose.ui.jw.model.VideoEvent
import com.jwplayer.compose.ui.jw.model.VideoState
import com.jwplayer.pub.api.PlayerState

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun VideoPlayer(
  modifier: Modifier = Modifier,
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
  videoState: VideoState,
  handleEvent: (event: VideoEvent) -> Unit
) {
  val context = LocalContext.current

  Box(
    modifier = modifier.background(Color.Black)
  ) {
    var controlsVisible by remember {
      mutableStateOf(
        true
      )
    }

    val controlsClickLabel = if (controlsVisible) {
      R.string.label_hide_controls
    } else {
      R.string.label_display_controls
    }

    Playback(
      modifier = Modifier
        .fillMaxSize()
        .clickable(
          onClickLabel = stringResource(id = controlsClickLabel)
        ) {
          controlsVisible = !controlsVisible
        },
      lifecycleOwner = lifecycleOwner,
      videoState = videoState,
      handleEvent = handleEvent,
      context = context
    )

    val alphaAnimation by animateFloatAsState(
      targetValue = if (controlsVisible) 0.7f else 0f,
      animationSpec = if (controlsVisible) {
        tween(delayMillis = 0)
      } else tween(delayMillis = 750)
    )

    Controls(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .alpha(alphaAnimation),
      playerState = videoState.playerState,
      togglePlayingState = {
        handleEvent(VideoEvent.ToggleStatus)
      }
    )
  }
}
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Preview
@Composable
fun Preview_VideoPlayer() {
  MaterialTheme {
    VideoPlayer(
      modifier = Modifier.fillMaxSize(),
      videoState = VideoState(),
      handleEvent = { }
    )
  }
}