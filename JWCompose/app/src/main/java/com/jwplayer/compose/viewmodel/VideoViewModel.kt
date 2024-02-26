package com.jwplayer.compose.viewmodel

import androidx.lifecycle.ViewModel
import com.jwplayer.compose.ui.jw.model.VideoEvent
import com.jwplayer.compose.ui.jw.model.VideoState
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.PlayerState
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.configuration.UiConfig
import com.jwplayer.pub.api.events.*
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents
import kotlinx.coroutines.flow.MutableStateFlow

class VideoViewModel : ViewModel(),
  VideoPlayerEvents.OnPlayListener,
  VideoPlayerEvents.OnReadyListener,
  VideoPlayerEvents.OnPauseListener,
  VideoPlayerEvents.OnIdleListener,
  VideoPlayerEvents.OnErrorListener,
  VideoPlayerEvents.OnSetupErrorListener{

  val uiState = MutableStateFlow(VideoState())
  private lateinit var jwPlayer: JWPlayer

  fun handleEvent(videoEvent: VideoEvent) {
    when (videoEvent) {
      is VideoEvent.PlayerAsyncInitialized -> {
        jwPlayer = videoEvent.jwPlayer
        initializeListeners(videoEvent.jwPlayer)
        setupPlayer(videoEvent.jwPlayer)
      }
      is VideoEvent.ToggleStatus -> togglePlayerStatus()
    }
  }

  private fun togglePlayerStatus() {
    if (jwPlayer.state == PlayerState.IDLE || jwPlayer.state == PlayerState.PAUSED) {
      jwPlayer.play()
    }

    if (jwPlayer.state == PlayerState.PLAYING) {
      jwPlayer.pause()
    }
  }

  private fun initializeListeners(jwPlayer: JWPlayer) {
    jwPlayer.addListener(EventType.READY, this)
    jwPlayer.addListener(EventType.PLAY, this)
    jwPlayer.addListener(EventType.PAUSE, this)
    jwPlayer.addListener(EventType.IDLE, this)
    jwPlayer.addListener(EventType.ERROR, this)
    jwPlayer.addListener(EventType.SETUP_ERROR, this)
  }

  override fun onCleared() {
    super.onCleared()
    jwPlayer.removeListener(EventType.READY, this)
    jwPlayer.removeListener(EventType.PLAY, this)
    jwPlayer.removeListener(EventType.PAUSE, this)
    jwPlayer.removeListener(EventType.IDLE, this)
    jwPlayer.removeListener(EventType.ERROR, this)
    jwPlayer.removeListener(EventType.SETUP_ERROR, this)
  }

  private fun setupPlayer(jwPlayer: JWPlayer) {
    val playerConfig = PlayerConfig.Builder()
      .playlistUrl("https://cdn.jwplayer.com/v2/playlists/3jBCQ2MI?format=json")
      .uiConfig(UiConfig.Builder()
        .hideAllControls()
        .build()
      )
      .build()

    jwPlayer.setup(playerConfig)
  }

  override fun onReady(event: ReadyEvent) {
    uiState.value = uiState.value.copy(
      playerState = PlayerState.IDLE
    )
  }

  override fun onPlay(event: PlayEvent) {
    uiState.value = uiState.value.copy(
      playerState = PlayerState.PLAYING
    )
  }

  override fun onPause(event: PauseEvent) {
    uiState.value = uiState.value.copy(
      playerState = PlayerState.PAUSED
    )
  }

  override fun onIdle(event: IdleEvent) {
    uiState.value = uiState.value.copy(
      playerState = PlayerState.IDLE
    )
  }

  override fun onError(event: ErrorEvent) {
    uiState.value = uiState.value.copy(
      playerState = PlayerState.ERROR
    )
  }

  override fun onSetupError(p0: SetupErrorEvent?) {
    uiState.value = uiState.value.copy(
      playerState = PlayerState.ERROR
    )
  }
}