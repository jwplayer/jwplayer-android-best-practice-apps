package com.jwplayer.offlinedrmdemo.kotlin

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jwplayer.offlinedrmdemo.BuildConfig
import com.jwplayer.offlinedrmdemo.R
import com.jwplayer.offlinedrmdemo.utils.JsonDownloader
import com.jwplayer.offlinedrmdemo.utils.JsonDownloader.JsonDownloadListener
import com.jwplayer.offlinedrmdemo.utils.JsonParser
import com.jwplayer.offlinedrmdemo.utils.NetworkTracker
import com.jwplayer.offlinedrmdemo.utils.NetworkTracker.NetworkStatusChangedListener
import com.jwplayer.offlinedrmdemo.utils.TokenSignedUrlGenerator
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.license.LicenseUtil
import com.jwplayer.pub.api.media.playlists.PlaylistItem
import com.jwplayer.pub.api.offline.MediaDownloadOption
import com.jwplayer.pub.api.offline.MediaDownloadResultListener
import com.jwplayer.pub.api.offline.OfflineDownloadFactory
import com.jwplayer.pub.api.offline.OfflineDownloadManager
import com.jwplayer.pub.view.JWPlayerView
import okhttp3.OkHttpClient

class MainActivityKt : AppCompatActivity(),
  MediaDownloadResultListener,
  NetworkStatusChangedListener,
  JsonDownloadListener {

  // Player
  private lateinit var mPlayerView: JWPlayerView
  private lateinit var mPlayer: JWPlayer
  private lateinit var mOfflineDownloadManager: OfflineDownloadManager

  // Online or Offline status view
  private lateinit var mOnline: TextView

  // Download status of DRM content
  private lateinit var mDownloaded: TextView

  // Video and audio download options UI
  private lateinit var mOptionsContainer: ViewGroup
  private lateinit var mVideoOptions: RadioGroup
  private lateinit var mAudioOptions: RadioGroup

  // Tracks current status of the network
  private lateinit var mNetworkTracker: NetworkTracker

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
    // [OR] change in app-level build.gradle
    // [OR] set JWPLAYER_LICENSE_KEY as environment variable
    LicenseUtil().setLicenseKey(applicationContext, BuildConfig.JWPLAYER_LICENSE_KEY)

    mPlayerView = findViewById(R.id.player)
    mOnline = findViewById(R.id.online)
    mDownloaded = findViewById(R.id.downloaded)
    mOptionsContainer = findViewById(R.id.options_container)
    mVideoOptions = findViewById(R.id.video_options)
    mAudioOptions = findViewById(R.id.audio_options)

    // Get the OfflineDownloadManager
    mOfflineDownloadManager = OfflineDownloadFactory.getOfflineDownloadManager(this)

    // Setup Listener and Start Service
    mOfflineDownloadManager.setMediaDownloadResultListener(this)
    mOfflineDownloadManager.startService(this)
    mNetworkTracker = NetworkTracker(applicationContext, this)

    findViewById<View>(R.id.prepare).setOnClickListener { v: View? ->
      prepareDownload()
    }

    findViewById<View>(R.id.setup).setOnClickListener { v: View? ->
      mPlayerView.setVisibility(View.VISIBLE)
      mPlayer = mPlayerView.getPlayer(this)
      setupPlayer(mPlayer)
    }

    findViewById<View>(R.id.download).setOnClickListener { v: View? ->
      downloadContent()
    }

    findViewById<View>(R.id.removeDownload).setOnClickListener { v: View ->
      // Removes the downloaded media from the device
      mOfflineDownloadManager.removeDownload(this, MEDIA_ID)
      mDownloaded.setText(R.string.not_downloaded)
      findViewById<View>(R.id.prepare).visibility = View.VISIBLE
      v.visibility = View.GONE
    }

    findViewById<View>(R.id.removeDownload).visibility = if (mOfflineDownloadManager.isDownloaded(MEDIA_ID)) View.VISIBLE else View.GONE

    // Update the downloaded status based on what the DrmDownloadManager knows
    mDownloaded.setText(if (mOfflineDownloadManager.isDownloaded(MEDIA_ID)) R.string.downloaded else R.string.not_downloaded)
  }


  private fun setupPlayer(jwPlayer: JWPlayer) {
    if (mNetworkTracker.isOnline) {
      // we are online, setup with Online DRM using the Delivery API URL
      Toast.makeText(this, R.string.using_delivery_api, Toast.LENGTH_SHORT).show()
      val config = PlayerConfig.Builder()
        .playlistUrl(TokenSignedUrlGenerator.get(MEDIA_ID, POLICY_ID))
        .build()
      jwPlayer.setup(config)
    } else {
      // we are offline, if the content is downloaed setup with Offline DRM
      val isDownloaded = mOfflineDownloadManager.isDownloaded(MEDIA_ID)
      if (isDownloaded) {
        Toast.makeText(this, R.string.using_offline_drm, Toast.LENGTH_SHORT).show()
        val playlistItem = mutableListOf<PlaylistItem>()
        mOfflineDownloadManager.getDownloadedPlaylistItem(MEDIA_ID)?.let { playlistItem.add(it) }

        jwPlayer.setup(
          PlayerConfig.Builder()
            .playlist(playlistItem)
            .build()
        )
      } else {
        // We are offline and no content is available
        Toast.makeText(this, R.string.no_offline_content, Toast.LENGTH_SHORT).show()
        jwPlayer.setup(PlayerConfig.Builder().build())
      }
    }
  }

  private fun downloadContent() {
    // You can't download content when offline
    if (!mNetworkTracker.isOnline) {
      Toast.makeText(this, R.string.offline, Toast.LENGTH_SHORT).show()
      return
    }

    if (mOfflineDownloadManager.isDownloaded(MEDIA_ID)) {
      Toast.makeText(this, "Already downloaded", Toast.LENGTH_SHORT).show()
      return
    }

    // User must select a video and audio rendition to download
    if (findViewById<View?>(mVideoOptions.checkedRadioButtonId) == null ||
      findViewById<View?>(mAudioOptions.checkedRadioButtonId) == null
    ) {
      Toast.makeText(this, R.string.must_select_media, Toast.LENGTH_SHORT).show()
      return
    }

    val video = findViewById<View>(mVideoOptions.checkedRadioButtonId).tag as MediaDownloadOption
    val audio = findViewById<View>(mAudioOptions.checkedRadioButtonId).tag as MediaDownloadOption

    // Download selected media for Offline DRM
    mOfflineDownloadManager.downloadMedia(this, video, audio)
    mOptionsContainer.visibility = View.GONE
    mVideoOptions.removeAllViews()
    mAudioOptions.removeAllViews()
  }

  override fun onMediaDownloadFailed(exception: Exception) {
    // Something went wrong when downloading
    Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show()
    exception.printStackTrace()
    mDownloaded.setText(R.string.not_downloaded)
    findViewById<View>(R.id.prepare).visibility = View.VISIBLE
  }

  override fun onDownloadOptionsAvailable(
    videoOptions: List<MediaDownloadOption>,
    audioOptions: List<MediaDownloadOption>,
    textOptions: List<MediaDownloadOption>
  ) {
    findViewById<View>(R.id.progress).visibility = View.GONE
    // Download options are available, show them to the user
    mOptionsContainer.visibility = View.VISIBLE
    fillRadioGroup(videoOptions, mVideoOptions)
    fillRadioGroup(audioOptions, mAudioOptions)
  }

  private fun prepareDownload() {
    // If we are offline, we can't prepare downloads of Offline DRM content
    if (!mNetworkTracker.isOnline) {
      Toast.makeText(this, R.string.offline, Toast.LENGTH_SHORT).show()
      return
    }

    // If content is already downloaded, no reason to download again
    val isDownloaded = mOfflineDownloadManager.isDownloaded(MEDIA_ID)
    if (isDownloaded) {
      Toast.makeText(this, R.string.already_downloaded, Toast.LENGTH_SHORT).show()
    } else {
      // Content is not downloaded, request the JSON from the Delivery API
      findViewById<View>(R.id.prepare).visibility = View.GONE
      findViewById<View>(R.id.progress).visibility = View.VISIBLE
      Toast.makeText(this, R.string.preparing_download, Toast.LENGTH_LONG).show()
      JsonDownloader(this, OkHttpClient())
        .execute(TokenSignedUrlGenerator.get(MEDIA_ID, POLICY_ID))
    }
  }

  private fun fillRadioGroup(options: List<MediaDownloadOption>, container: RadioGroup) {
    container.removeAllViews()
    for (option in options) {
      val radioButton = RadioButton(this)
      radioButton.text = option.label
      radioButton.tag = option
      radioButton.id = View.generateViewId()
      container.addView(radioButton)
    }
  }

  override fun onDownloadComplete(mediaId: String) {
    mOptionsContainer.visibility = View.GONE
    mVideoOptions.removeAllViews()
    mAudioOptions.removeAllViews()
    mDownloaded.setText(R.string.downloaded)
    findViewById<View>(R.id.removeDownload).visibility = View.VISIBLE
  }

  override fun onDownloadUpdate(mediaId: String, percentage: Float) {
    mDownloaded.text = "Content Downloading: $percentage%"
  }

  override fun onNetworkStatusChanged(isOnline: Boolean) {
    mOnline.setText(if (isOnline) R.string.online else R.string.offline)
  }

  override fun onJsonDownloadComplete(playlistJson: String) {
    val item = JsonParser.parseJson(playlistJson)
    if (item != null) {
      mOfflineDownloadManager.prepareMediaDownload(this, item)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    mNetworkTracker.destroy()
    // When the activity is destroyed you must release all Offline DRM resources
    OfflineDownloadFactory.destroyAll()
  }

  companion object {
    // DRM resource identifiers
    private const val POLICY_ID = "RbFUSrSU"
    private const val MEDIA_ID = "379yuBhA"
  }
}