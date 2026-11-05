package com.jwplayer.demo.jwcompose.ui.jw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jwplayer.demo.jwcompose.R
import com.jwplayer.pub.api.PlayerState

/** A minimal Compose control bar: a single play/pause button. */
@Composable
fun Controls(
  modifier: Modifier = Modifier,
  playerState: PlayerState,
  onTogglePlayback: () -> Unit
) {
  Box(
    modifier = modifier
      .background(MaterialTheme.colorScheme.surface)
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    val isPlaying = playerState == PlayerState.PLAYING
    FilledIconButton(
      onClick = onTogglePlayback,
      enabled = playerState != PlayerState.BUFFERING,
      modifier = Modifier.size(72.dp)
    ) {
      Icon(
        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
        contentDescription = stringResource(id = if (isPlaying) R.string.cd_pause else R.string.cd_play),
        modifier = Modifier.size(48.dp)
      )
    }
  }
}

@Preview
@Composable
private fun ControlsPreview() {
  MaterialTheme {
    Controls(
      modifier = Modifier.fillMaxWidth(),
      playerState = PlayerState.PAUSED,
      onTogglePlayback = { }
    )
  }
}
