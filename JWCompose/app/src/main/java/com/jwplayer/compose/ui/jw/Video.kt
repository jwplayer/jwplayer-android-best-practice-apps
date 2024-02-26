package com.jwplayer.compose.ui.jw

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jwplayer.compose.viewmodel.VideoViewModel

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun Video(
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
  val viewModel: VideoViewModel = viewModel()

  VideoPlayer(
    modifier = Modifier.fillMaxSize(),
    videoState = viewModel.uiState.collectAsState().value,
    lifecycleOwner = lifecycleOwner,
    handleEvent = viewModel::handleEvent
  )
}