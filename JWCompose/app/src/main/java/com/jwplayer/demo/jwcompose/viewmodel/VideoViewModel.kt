package com.jwplayer.demo.jwcompose.viewmodel

import androidx.lifecycle.ViewModel
import com.jwplayer.demo.jwcompose.ui.jw.VideoState
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.PlayerState
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.configuration.UiConfig
import com.jwplayer.pub.api.events.CompleteEvent
import com.jwplayer.pub.api.events.ErrorEvent
import com.jwplayer.pub.api.events.EventType
import com.jwplayer.pub.api.events.IdleEvent
import com.jwplayer.pub.api.events.PauseEvent
import com.jwplayer.pub.api.events.PlayEvent
import com.jwplayer.pub.api.events.ReadyEvent
import com.jwplayer.pub.api.events.SetupErrorEvent
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Turns player events into [VideoState] for the Compose UI.
 *
 * The ViewModel never creates the player and only holds it between [attach] and [detach], so it
 * can't keep a player (and its Activity) alive after the composable that owns it is gone.
 */
class VideoViewModel : ViewModel(),
  VideoPlayerEvents.OnPlayListener,
  VideoPlayerEvents.OnReadyListener,
  VideoPlayerEvents.OnPauseListener,
  VideoPlayerEvents.OnIdleListener,
  VideoPlayerEvents.OnCompleteListener,
  VideoPlayerEvents.OnErrorListener,
  VideoPlayerEvents.OnSetupErrorListener {

  private val _uiState = MutableStateFlow(VideoState())
  val uiState: StateFlow<VideoState> = _uiState.asStateFlow()

  private var player: JWPlayer? = null

  fun attach(newPlayer: JWPlayer) {
    if (player === newPlayer) return
    player?.let(::detach)
    player = newPlayer
    newPlayer.addListeners(this, *EVENTS)
    newPlayer.setup(
      PlayerConfig.Builder()
        .playlistUrl("https://cdn.jwplayer.com/v2/playlists/9L8aDjaU")
        // Hide the built-in controls; the Compose Controls() replace them.
        .uiConfig(UiConfig.Builder().hideAllControls().build())
        .build()
    )
  }

  fun detach(oldPlayer: JWPlayer) {
    oldPlayer.removeListeners(this, *EVENTS)
    if (player === oldPlayer) player = null
  }

  fun togglePlayback() {
    val p = player ?: return
    when (p.state) {
      PlayerState.PLAYING -> p.pause()
      else -> p.play()
    }
  }

  override fun onCleared() {
    player?.let(::detach)
    super.onCleared()
  }

  private fun update(state: PlayerState) {
    _uiState.value = _uiState.value.copy(playerState = state)
  }

  override fun onReady(event: ReadyEvent) = update(PlayerState.IDLE)
  override fun onPlay(event: PlayEvent) = update(PlayerState.PLAYING)
  override fun onPause(event: PauseEvent) = update(PlayerState.PAUSED)
  override fun onIdle(event: IdleEvent) = update(PlayerState.IDLE)
  // When an item completes there is no PAUSE; show the play icon. If the playlist advances, the
  // next PLAY switches it back, so after the last item it correctly stays on play.
  override fun onComplete(event: CompleteEvent) = update(PlayerState.IDLE)
  override fun onError(event: ErrorEvent) = update(PlayerState.ERROR)
  override fun onSetupError(event: SetupErrorEvent?) = update(PlayerState.ERROR)

  private companion object {
    val EVENTS = arrayOf(
      EventType.READY, EventType.PLAY, EventType.PAUSE,
      EventType.IDLE, EventType.COMPLETE, EventType.ERROR, EventType.SETUP_ERROR
    )
  }
}
