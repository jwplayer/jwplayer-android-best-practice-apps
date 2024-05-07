package com.jwplayer.compose.ui.jw.model

import com.jwplayer.pub.api.PlayerState
import com.jwplayer.pub.api.configuration.PlayerConfig

data class VideoState(
    val playerConfig: PlayerConfig? = null,
    val playerState: PlayerState = PlayerState.BUFFERING
)