package com.jwplayer.drmdemo.kotlin

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jwplayer.drmdemo.BuildConfig
import com.jwplayer.drmdemo.R
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.JWPlayer.PlayerInitializationListener
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.events.ErrorEvent
import com.jwplayer.pub.api.events.EventType
import com.jwplayer.pub.api.events.ReadyEvent
import com.jwplayer.pub.api.events.SetupErrorEvent
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.OnReadyListener
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.OnSetupErrorListener
import com.jwplayer.pub.api.license.LicenseUtil
import com.jwplayer.pub.api.media.playlists.PlaylistItem
import com.jwplayer.pub.view.JWPlayerView

class MainActivityKt : AppCompatActivity() {

  private lateinit var playerView: JWPlayerView
  private lateinit var status: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    playerView = findViewById(R.id.jwplayerview)
    status = findViewById(R.id.status)

    // INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
    // [OR] change in app-level build.gradle
    // [OR] set JWPLAYER_LICENSE_KEY as environment variable
    LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY)

    playerView.getPlayerAsync(this, this, PlayerInitializationListener { jwPlayer: JWPlayer ->
      attachListeners(jwPlayer)

      val streamURL = ""
      val licenseURL = ""

      if (streamURL.isEmpty()) {
        status.append("\nStream and license URL are required")
        return@PlayerInitializationListener
      }

      setupPlayer(jwPlayer, streamURL, licenseURL)
    })
  }

  private fun attachListeners(player: JWPlayer) {
    player.addListener(EventType.SETUP_ERROR, OnSetupErrorListener { setupErrorEvent: SetupErrorEvent -> status.append("\n" + setupErrorEvent.message)})
    player.addListener(EventType.ERROR, VideoPlayerEvents.OnErrorListener { errorEvent: ErrorEvent -> status.append("\n" + errorEvent.message) })
    player.addListener(EventType.READY, OnReadyListener { readyEvent: ReadyEvent -> status.append("\nREADY") })
  }

  private fun setupPlayer(player: JWPlayer, streamURL: String, licenseURL: String) {
    // For self hosted content requiring header authorization
    val httpHeaders: MutableMap<String, String> = HashMap()
    httpHeaders["sample_header_key"] = "sample_header_value"

    // Additional properties to pass to your DRM License server
    val requestProperties: MutableMap<String, String> = HashMap()
    requestProperties["customData"] = "sample_request_prop_value"

    val playlist = mutableListOf<PlaylistItem>()
    playlist.add(
      PlaylistItem.Builder()
        .file(streamURL)
        .httpHeaders(httpHeaders)
        .mediaDrmCallback(WidevineCallbackKt(licenseURL, requestProperties))
        .build()
    )

    player.setup(
      PlayerConfig.Builder()
        .playlist(playlist)
        .build()
    )
  }
}