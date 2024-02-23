package com.jwplayer.customui

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.JWPlayer.PlayerInitializationListener
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.configuration.UiConfig
import com.jwplayer.pub.api.events.EventType
import com.jwplayer.pub.api.events.FirstFrameEvent
import com.jwplayer.pub.api.events.FullscreenEvent
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.OnFirstFrameListener
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.OnFullscreenListener
import com.jwplayer.pub.api.license.LicenseUtil
import com.jwplayer.pub.api.media.playlists.PlaylistItem
import com.jwplayer.pub.view.JWPlayerView

class JWPlayerViewExample : AppCompatActivity(), OnFullscreenListener, OnFirstFrameListener {

  private lateinit var mPlayerView: JWPlayerView
  private lateinit var mPlayer: JWPlayer

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_jwplayerview)
    WebView.setWebContentsDebuggingEnabled(true)
    // TODO: Add your license key
    LicenseUtil().setLicenseKey(this, "License")
    mPlayerView = findViewById(R.id.jwplayer)

    mPlayerView.getPlayerAsync(this, this, PlayerInitializationListener { jwPlayer: JWPlayer? ->
      mPlayer = jwPlayer ?: error("Player should not be null")
      setupPlayer()
    })
  }

  override fun onPause() {
    super.onPause()
  }

  override fun onDestroy() {
    super.onDestroy()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    if (!mPlayer.isInPictureInPictureMode) {
      val isFullscreen = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
      mPlayer.setFullscreen(isFullscreen, true)
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    // Exit fullscreen when the user pressed the Back button
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (mPlayer.fullscreen) {
        mPlayer.setFullscreen(false, true)
        return false
      }
    }
    return super.onKeyDown(keyCode, event)
  }

  override fun onFirstFrame(firstFrameEvent: FirstFrameEvent?) {
  }

  override fun onFullscreen(fullscreenEvent: FullscreenEvent) {
    val actionBar = supportActionBar
    if (actionBar != null) {
      if (fullscreenEvent.fullscreen) {
        actionBar.hide()
      } else {
        actionBar.show()
      }
    }
  }

  private fun setupPlayer() {
    // Handle hiding/showing of ActionBar
    mPlayer.addListener(EventType.FULLSCREEN, this@JWPlayerViewExample)

    // Keep the screen on during playback
    KeepScreenOnHandler(mPlayer, window)

    val playlist: MutableList<PlaylistItem> = ArrayList()
    playlist.add(
      PlaylistItem.Builder()
        .file("https://content.jwplatform.com/manifests/mkZVAqxV.m3u8")
        .image("https://assets-jpcust.jwpsrv.com/thumbs/mkZVAqxV-720.jpg")
        .mediaId("mkZVAqxV")
        .description("Caminandes 1: Llama Drama (2013)")
        .title("Caminandes 1: Llama Drama")
        .build()
    )
    playlist.add(
      PlaylistItem.Builder()
        .file("https://content.jwplatform.com/manifests/t6Kk91mw.m3u8")
        .image("https://assets-jpcust.jwpsrv.com/thumbs/t6Kk91mw-720.jpg")
        .mediaId("t6Kk91mw")
        .description("Caminandes 2: Gran Dillama (2013)")
        .title("Caminandes 2: Gran Dillama")
        .startTime(20.0)
        .build()
    )

    // Create a JWPlayerConfig
    // Load a media source
    val config = PlayerConfig.Builder()
      .playlist(playlist)
      .uiConfig(
        UiConfig.Builder()
          .displayAllControls()
          .hide(UiGroup.NEXT_UP)
          .build()
      )
      .autostart(true)
      .build()
    // Call setup before binding the ViewModels because setup updates the ViewModels
    mPlayer.setup(config)


    // We create a MyControls ViewGroup in which we can control the positioning of the Views
    val controls = MyControls(ContextThemeWrapper(this, R.style.AppTheme))
    val params = FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    )
    controls.layoutParams = params
    mPlayerView.addView(controls)
    controls.bind(mPlayer, this)
  }
}
