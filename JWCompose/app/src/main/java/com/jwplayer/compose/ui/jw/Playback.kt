package com.jwplayer.compose.ui.jw

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.jwplayer.compose.ui.jw.model.VideoEvent
import com.jwplayer.compose.ui.jw.model.VideoState
import com.jwplayer.pub.view.JWPlayerView

@Composable
fun Playback(
  modifier: Modifier = Modifier,
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
  videoState: VideoState,
  handleEvent: (event: VideoEvent) -> Unit,
  context: Context
) {
  DisposableEffect(
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = {
        JWPlayerView(context).apply {
          getPlayerAsync(context, lifecycleOwner) { jwPlayer ->
            handleEvent(VideoEvent.PlayerAsyncInitialized(jwPlayer))
          }
        }
      },
      update = { playerView ->
        /**
         * Any operation for JWPlayerView after inflated in the view.
         */
        // videoState.playerConfig?.let { playerView.player.setup(it) }
      })
  ) {
    onDispose {

    }
  }
}