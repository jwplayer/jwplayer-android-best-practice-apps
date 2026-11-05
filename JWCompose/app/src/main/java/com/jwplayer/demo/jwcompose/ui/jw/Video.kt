package com.jwplayer.demo.jwcompose.ui.jw

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jwplayer.demo.jwcompose.viewmodel.VideoViewModel

/** Connects [VideoViewModel] (player state and actions) to the stateless [VideoPlayer] UI. */
@Composable
fun Video(modifier: Modifier = Modifier) {
  val viewModel: VideoViewModel = viewModel()
  val videoState by viewModel.uiState.collectAsState()

  VideoPlayer(
    modifier = modifier,
    videoState = videoState,
    onPlayerCreated = viewModel::attach,
    onPlayerDisposed = viewModel::detach,
    onTogglePlayback = viewModel::togglePlayback
  )
}
