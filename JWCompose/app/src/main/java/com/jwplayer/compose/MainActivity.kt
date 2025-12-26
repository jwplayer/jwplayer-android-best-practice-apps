package com.jwplayer.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.jwplayer.compose.ui.jw.Video
import com.jwplayer.compose.ui.theme.JWComposeTheme
import com.jwplayer.pub.api.license.LicenseUtil

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Enable edge-to-edge for API 35
    WindowCompat.setDecorFitsSystemWindows(window, false)

    LicenseUtil().setLicenseKey(this, YOUR_LICENSE_KEY)

    setContent {
      JWComposeTheme {
        Video(
          lifecycleOwner = this,
          modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
        )
      }
    }
  }
}

@Composable
fun Greeting(name: String) {
  Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  JWComposeTheme {
    Greeting("Android")
  }
}