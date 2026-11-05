package com.jwplayer.demo.jwcompose.ui.jw

import com.jwplayer.pub.api.PlayerState

data class VideoState(
  val playerState: PlayerState = PlayerState.BUFFERING
)
