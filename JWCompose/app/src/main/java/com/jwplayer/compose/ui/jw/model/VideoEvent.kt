package com.jwplayer.compose.ui.jw.model

import com.jwplayer.pub.api.JWPlayer

sealed class VideoEvent {
    data class PlayerAsyncInitialized(
        val jwPlayer: JWPlayer
    ): VideoEvent()

    object ToggleStatus: VideoEvent()
}