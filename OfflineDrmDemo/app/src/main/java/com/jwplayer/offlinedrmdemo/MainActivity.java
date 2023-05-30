package com.jwplayer.offlinedrmdemo;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.api.offline.MediaDownloadOption;
import com.jwplayer.pub.api.offline.MediaDownloadResultListener;
import com.jwplayer.pub.api.offline.OfflineDownloadFactory;
import com.jwplayer.pub.api.offline.OfflineDownloadManager;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity
        implements MediaDownloadResultListener,
                   NetworkTracker.NetworkStatusChangedListener,
                   JsonDownloader.JsonDownloadListener {

    // DRM resource identifiers
    private static final String POLICY_ID = "RbFUSrSU";
    private static final String MEDIA_ID = "379yuBhA";

    // Player
    private JWPlayerView mPlayerView;
    private JWPlayer mPlayer;
    private OfflineDownloadManager mOfflineDownloadManager;

    // Online or Offline status view
    private TextView mOnline;
    // Download status of DRM content
    private TextView mDownloaded;

    // Video and audio download options UI
    private ViewGroup mOptionsContainer;
    private RadioGroup mVideoOptions;
    private RadioGroup mAudioOptions;

    // Tracks current status of the network
    private NetworkTracker mNetworkTracker;

    // Downloads JSON from the Delivery API (DAPI)
    private JsonDownloader mJsonDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new LicenseUtil().setLicenseKey(getApplicationContext(),YOUR_LICENSE_KEY);
        mPlayerView = findViewById(R.id.player);
        mOnline = findViewById(R.id.online);
        mDownloaded = findViewById(R.id.downloaded);
        mOptionsContainer = findViewById(R.id.options_container);
        mVideoOptions = findViewById(R.id.video_options);
        mAudioOptions = findViewById(R.id.audio_options);

        // Get the OfflineDownloadManager
        mOfflineDownloadManager = OfflineDownloadFactory.getOfflineDownloadManager(this);
        mOfflineDownloadManager.startService(this);

        mNetworkTracker = new NetworkTracker(getApplicationContext(), this);
        mJsonDownloader = new JsonDownloader(this, new OkHttpClient());


        findViewById(R.id.prepare).setOnClickListener(v -> {
            // If we are offline, we can't prepare downloads of Offline DRM content
            if (!mNetworkTracker.isOnline()) {
                Toast.makeText(this, R.string.offline, Toast.LENGTH_SHORT).show();
                return;
            }

            // If content is already downloaded, no reason to download again
            boolean isDownloaded = mOfflineDownloadManager.isDownloaded(MEDIA_ID);
            if (isDownloaded) {
                Toast.makeText(this, R.string.already_downloaded, Toast.LENGTH_SHORT).show();
            } else {
                // Content is not downloaded, request the JSON from the Delivery API
                Toast.makeText(this, R.string.preparing_download, Toast.LENGTH_SHORT).show();
                mJsonDownloader.execute(TokenSignedUrlGenerator.get(MEDIA_ID, POLICY_ID));
            }
        });

        findViewById(R.id.setup).setOnClickListener(v -> {
            if (mNetworkTracker.isOnline()) {
                // we are online, setup with Online DRM using the Delivery API URL
                Toast.makeText(this, R.string.using_delivery_api, Toast.LENGTH_SHORT)
                     .show();
                mPlayerView.setVisibility(View.VISIBLE);
                mPlayer = mPlayerView.getPlayer(this);
                PlayerConfig config = new PlayerConfig.Builder()
                        .playlistUrl(TokenSignedUrlGenerator.get(MEDIA_ID, POLICY_ID))
                        .build();
                mPlayer.setup(config);
            } else {
                // we are offline, if the content is downloaed setup with Offline DRM
                boolean isDownloaded = mOfflineDownloadManager.isDownloaded(MEDIA_ID);
                if (isDownloaded) {
                    Toast.makeText(this, R.string.using_offline_drm, Toast.LENGTH_SHORT)
                         .show();
                    mPlayerView.setVisibility(View.VISIBLE);
                    mPlayer = mPlayerView.getPlayer(this);
                    mPlayer.setup(new PlayerConfig.Builder()
                                          .playlist(new ArrayList<PlaylistItem>() {{
                                              // Use the DrmDownloadManager to get an Offline DRM PlaylistItem
                                              add(mOfflineDownloadManager.getDownloadedPlaylistItem(
                                                      MEDIA_ID));
                                          }})
                                          .build()
                    );
                } else {
                    // We are offline and no content is available
                    mPlayerView.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.no_offline_content, Toast.LENGTH_SHORT)
                         .show();
                }
            }
        });

        findViewById(R.id.download).setOnClickListener(v -> {
            // You can't download content when offline
            if (!mNetworkTracker.isOnline()) {
                Toast.makeText(this, R.string.offline, Toast.LENGTH_SHORT).show();
                return;
            }

            MediaDownloadOption video = (MediaDownloadOption) findViewById(mVideoOptions.getCheckedRadioButtonId()).getTag();
            MediaDownloadOption audio = (MediaDownloadOption) findViewById(mAudioOptions.getCheckedRadioButtonId()).getTag();
            // User must select a video and audio rendition to download
            if (video == null || audio == null) {
                Toast.makeText(this, R.string.must_select_media, Toast.LENGTH_SHORT).show();
                return;
            }
            // Download selected media for Offline DRM
            mOfflineDownloadManager.downloadMedia(this, video, audio);
        });

        findViewById(R.id.removeDownload).setOnClickListener(v -> {
            // Removes the downloaded media from the device
            mOfflineDownloadManager.removeDownload(this, MEDIA_ID);
            mDownloaded.setText(R.string.not_downloaded);
        });

        // Update the downloaded status based on what the DrmDownloadManager knows
        mDownloaded.setText(mOfflineDownloadManager.isDownloaded(MEDIA_ID) ? R.string.downloaded : R.string.not_downloaded);
    }

    @Override
    public void onMediaDownloadFailed(Exception exception) {
        // Something went wrong when downloading
        Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show();
        exception.printStackTrace();
        mDownloaded.setText(R.string.not_downloaded);
    }

    @Override
    public void onDownloadOptionsAvailable(List<MediaDownloadOption> videoOptions,
                                           List<MediaDownloadOption> audioOptions,
                                           List<MediaDownloadOption> textOptions) {
        // Download options are available, show them to the user
        mOptionsContainer.setVisibility(View.VISIBLE);
        fillRadioGroup(videoOptions, mVideoOptions);
        fillRadioGroup(audioOptions, mAudioOptions);
    }

    private void fillRadioGroup(List<MediaDownloadOption> options, RadioGroup container) {
        container.removeAllViews();
        for (MediaDownloadOption option : options) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option.getLabel());
            radioButton.setTag(option);
            radioButton.setId(View.generateViewId());
            container.addView(radioButton);
        }
    }

    @Override
    public void onDownloadComplete(String mediaId) {
        mOptionsContainer.setVisibility(View.GONE);
        mVideoOptions.removeAllViews();
        mAudioOptions.removeAllViews();
        mDownloaded.setText(R.string.downloaded);
    }

    @Override
    public void onDownloadUpdate(String mediaId, float percentage) {
        mDownloaded.setText("Content Downloading: "+percentage+"%");
    }

    @Override
    public void onNetworkStatusChanged(boolean isOnline) {
        mOnline.setText(isOnline ? R.string.online : R.string.offline);
    }

    @Override
    public void onJsonDownloadComplete(String playlistJson) {
        PlaylistItem item = JsonParser.parseJson(playlistJson);
        if (item != null) {
            mOfflineDownloadManager.prepareMediaDownload(this, item, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNetworkTracker.destroy();
        // When the activity is destroyed you must release all Offline DRM resources
        OfflineDownloadFactory.destroyAll();
    }
}