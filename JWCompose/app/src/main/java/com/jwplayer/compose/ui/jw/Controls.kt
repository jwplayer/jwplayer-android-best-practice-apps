package com.jwplayer.compose.ui.jw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jwplayer.compose.R
import com.jwplayer.pub.api.PlayerState

@Composable
fun Controls(
  modifier: Modifier = Modifier,
  playerState: PlayerState,
  togglePlayingState: () -> Unit
) {
  Box(
    modifier = modifier
      .background(MaterialTheme.colors.surface)
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    FilledIconButton(
      onClick = {
        togglePlayingState()
      },
      enabled = playerState != PlayerState.BUFFERING,
      modifier = Modifier.size(72.dp),
      colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colors.primary,
        contentColor = Color.White
      )
    ) {
      val icon = if (playerState == PlayerState.PLAYING) {
        Icons.Default.Pause
      } else {
        Icons.Default.PlayArrow
      }

      val description = if (playerState == PlayerState.PLAYING) {
        stringResource(id = R.string.cd_pause)
      } else {
        stringResource(id = R.string.cd_play)
      }

      Icon(
        imageVector = icon,
        contentDescription = description,
        modifier = Modifier.size(48.dp)
      )
    }
  }
}

@Preview
@Composable
fun Preview_Controls() {
  MaterialTheme {
    Controls(
      modifier = Modifier.fillMaxWidth(),
      playerState = PlayerState.PAUSED,
      togglePlayingState = { }
    )
  }
}