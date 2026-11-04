package com.jwplayer.demo.jwcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import com.jwplayer.demo.jwcompose.ui.jw.Video
import com.jwplayer.demo.jwcompose.ui.theme.JWComposeTheme
import com.jwplayer.pub.api.license.LicenseUtil

/**
 * The manifest declares `configChanges` for rotation and resizing, so this Activity (and the player
 * inside it) is not destroyed on a configuration change: Compose simply recomposes with the new size.
 */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
    // [OR] change in app-level build.gradle
    // [OR] set JWPLAYER_LICENSE_KEY as environment variable
    LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY)

    setContent {
      JWComposeTheme {
        Video(
          modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
        )
      }
    }
  }
}
