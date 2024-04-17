package com.jwplayer.demo.notificationsdemo.kotlin

import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jwplayer.demo.notificationsdemo.BuildConfig
import com.jwplayer.demo.notificationsdemo.R
import com.jwplayer.demo.notificationsdemo.utils.Sample
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.JWPlayer.PlayerInitializationListener
import com.jwplayer.pub.api.background.MediaServiceController
import com.jwplayer.pub.api.background.NotificationHelper
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.events.EventType
import com.jwplayer.pub.api.events.FirstFrameEvent
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.OnFirstFrameListener
import com.jwplayer.pub.api.license.LicenseUtil
import com.jwplayer.pub.view.JWPlayerView

class VideoActivity : AppCompatActivity(), PlayerInitializationListener, OnFirstFrameListener {
  /**
   * The JWPlayerView used for video playback.
   */
  private lateinit var mPlayerView: JWPlayerView
  private lateinit var mJWPlayer: JWPlayer
  private lateinit var mMediaServiceController: MediaServiceController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.video_activity_kt)

    // INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
    // [OR] change in app-level build.gradle
    // [OR] set JWPLAYER_LICENSE_KEY as environment variable
    LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY)
    mPlayerView = findViewById(R.id.demoJWPlayerView)
    mPlayerView.getPlayerAsync(this, this, this)
  }

  override fun onPause() {
    super.onPause()
    mJWPlayer.allowBackgroundAudio(true)
  }

  override fun onDestroy() {
    mMediaServiceController.unbindService()
    super.onDestroy()
  }

  override fun onPlayerInitialized(player: JWPlayer) {
    mJWPlayer = player

    // Create a JWPlayerConfig
    val playerConfig = PlayerConfig.Builder()
      .playlist(Sample.PLAYLIST)
      .build()

    // Create a new JWPlayerView
    mJWPlayer.addListener(EventType.FIRST_FRAME, this)
    mJWPlayer.setup(playerConfig)

    mMediaServiceController = MediaServiceController.Builder(this, mJWPlayer)
      .notificationHelper(customNotification())
      .build()
  }

  override fun onFirstFrame(firstFrameEvent: FirstFrameEvent) {
    // Only bind to the service if media has begun playback
    // You could also use onBeforePlay as your listener
    // if you wanted to start the service and notification earlier
    mMediaServiceController.bindService()
  }

  /**
   * Provide custom notification with resources or description.
   * Red more: https://docs.jwplayer.com/players/docs/android-enable-background-audio#customization
   */
  private fun customNotification(): NotificationHelper {
    val notificationManager: NotificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    return NotificationHelper.Builder(notificationManager)
      .notificationId(2005)
      .channelNameDisplayedToUser("My custom name displayed")
      .iconDrawableResource(R.drawable.ic_thumbs_up)
      .build()
  }
}
