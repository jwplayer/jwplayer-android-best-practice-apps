package com.jwplayer.demo.jwcompose.ui.jw

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.view.JWPlayerView

/**
 * Hosts a [JWPlayerView] in Compose.
 *
 * - The view and its player are created inside `remember`, so recomposition never creates a new
 *   player. (Configuration changes don't recreate the Activity either; see the manifest.)
 * - The player is tied to the Activity's lifecycle via `getPlayer(lifecycleOwner)`, which releases it
 *   when the Activity is destroyed.
 * - [onPlayerCreated] / [onPlayerDisposed] run once when this composable enters / leaves the
 *   composition, so listeners are added and removed exactly once.
 */
@Composable
fun Playback(
  modifier: Modifier = Modifier,
  onPlayerCreated: (JWPlayer) -> Unit,
  onPlayerDisposed: (JWPlayer) -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  val playerView = remember(context) { JWPlayerView(context) }
  val player = remember(playerView, lifecycleOwner) { playerView.getPlayer(lifecycleOwner) }

  DisposableEffect(player) {
    onPlayerCreated(player)
    onDispose { onPlayerDisposed(player) }
  }

  AndroidView(
    modifier = modifier,
    factory = { playerView }
  )
}
