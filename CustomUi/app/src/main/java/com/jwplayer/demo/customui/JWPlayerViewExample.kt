package com.jwplayer.demo.customui

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.JWPlayer.PlayerInitializationListener
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.configuration.UiConfig
import com.jwplayer.pub.api.events.EventType
import com.jwplayer.pub.api.events.FullscreenEvent
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.OnFullscreenListener
import com.jwplayer.pub.api.license.LicenseUtil
import com.jwplayer.pub.api.media.playlists.PlaylistItem
import com.jwplayer.pub.view.JWPlayerView

class JWPlayerViewExample : AppCompatActivity(), OnFullscreenListener {

  private lateinit var mPlayerView: JWPlayerView
  private lateinit var mPlayer: JWPlayer

  // Back leaves fullscreen. The JW Player SDK does no Back handling of its own, so the app does it
  // here. Enabled only while fullscreen (from the FULLSCREEN event), so otherwise Back behaves
  // normally. The manifest opts out of predictive back (enableOnBackInvokedCallback="false") so
  // this callback keeps working under targetSdk 36.
  private val exitFullscreenOnBack = object : OnBackPressedCallback(false) {
    override fun handleOnBackPressed() {
      mPlayer.setFullscreen(false, true)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_jwplayerview)

    // Handle status bar insets
    ViewCompat.setOnApplyWindowInsetsListener(
      window.decorView,
      OnApplyWindowInsetsListener { v: View?, windowInsets: WindowInsetsCompat? ->
        val statusBars = windowInsets!!.getInsets(WindowInsetsCompat.Type.statusBars())
        val navBar = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
        v!!.setPadding(0, statusBars.top, 0, navBar.bottom)
        windowInsets
      })

    // The license key is read from the JWPLAYER_LICENSE_KEY environment variable at build time
    LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY)
    mPlayerView = findViewById(R.id.jwplayer)
    onBackPressedDispatcher.addCallback(this, exitFullscreenOnBack)

    mPlayerView.getPlayerAsync(this, this, PlayerInitializationListener { jwPlayer: JWPlayer? ->
      mPlayer = jwPlayer ?: error("Player should not be null")
      setupPlayer()
    })
  }

  // No onPause/onDestroy overrides are needed: getPlayerAsync() binds the player to this
  // Activity's lifecycle, so the SDK pauses and releases the player for us.

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    // The player is created asynchronously; ignore rotations that happen before it exists.
    if (!::mPlayer.isInitialized) return
    if (!mPlayer.isInPictureInPictureMode) {
      val isFullscreen = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
      mPlayer.setFullscreen(isFullscreen, true)
    }
  }

  override fun onFullscreen(fullscreenEvent: FullscreenEvent) {
    exitFullscreenOnBack.isEnabled = fullscreenEvent.fullscreen
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
