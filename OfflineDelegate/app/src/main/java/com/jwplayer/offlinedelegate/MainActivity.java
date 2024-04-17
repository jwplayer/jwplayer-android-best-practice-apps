package com.jwplayer.offlinedelegate;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadService;

import com.jwplayer.offlinedelegate.exo.DemoDownloadService;
import com.jwplayer.offlinedelegate.exo.DemoUtil;
import com.jwplayer.offlinedelegate.exo.DownloadTracker;
import com.jwplayer.offlinedelegate.exo.OfflineDelegateImpl;
import com.jwplayer.pub.api.ExoConverter;
import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.captions.Caption;
import com.jwplayer.pub.api.media.captions.CaptionType;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;

@UnstableApi public class MainActivity extends AppCompatActivity
        implements NetworkTracker.NetworkStatusChangedListener, DownloadTracker.Listener {

    private NetworkTracker mNetworkTracker;

    private TextView mNetworkStatus;
    private TextView mContentStatus;
    private JWPlayerView mPlayerView;
    private JWPlayer mPlayer;

    private final PlaylistItem mOnlineSideloaded = new PlaylistItem.Builder()
            .mediaId("QUxmKbSS")
            .file("https://cdn.jwplayer.com/manifests/QUxmKbSS.m3u8")
            .title("Online Sideloaded")
            .tracks(new ArrayList<Caption>() {{
                add(new Caption.Builder()
                            .file("https://content.jwplatform.com/tracks/a2J6cY6H.srt")
                            .kind(CaptionType.CAPTIONS)
                            .label("English")
                            .build());
            }})
            .build();
    private final PlaylistItem mOfflineSideloaded = new PlaylistItem.Builder()
            .mediaId("3XnJSIm4")
            .title("Offline Sideloaded")
            .file("https://cdn.jwplayer.com/manifests/3XnJSIm4.m3u8")
            .tracks(new ArrayList<Caption>() {{
                add(new Caption.Builder()
                            .file("asset:///Sintel_en.srt")
                            .kind(CaptionType.CAPTIONS)
                            .label("English")
                            .build());
                add(new Caption.Builder()
                            .file("asset:///Sintel_es.srt")
                            .kind(CaptionType.CAPTIONS)
                            .label("Spanish")
                            .build());
                add(new Caption.Builder()
                            .file("asset:///Sintel_jp.srt")
                            .kind(CaptionType.CAPTIONS)
                            .label("Japanese")
                            .build());
            }})
            .build();
    private final PlaylistItem mInManifest = new PlaylistItem.Builder()
            .mediaId("70zwzhEM")
            .title("In Manifest")
            .file("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8")
            .build();

    private PlaylistItem mCurrentItem = mOnlineSideloaded;

    private OfflineDelegateImpl mOfflineDelegate;

    // ExoPlayer demo code
    private DownloadTracker downloadTracker;
    private RenderersFactory mRenderersFactory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNetworkStatus = findViewById(R.id.network_status);
        mContentStatus = findViewById(R.id.content_status);
        mPlayerView = findViewById(R.id.player);

        // TODO ask for user permissions or manually approve post notifications

        new LicenseUtil().setLicenseKey(this, YOUR_LICENSE_KEY);
        mNetworkTracker = new NetworkTracker(getApplicationContext(), this);

        mRenderersFactory = DemoUtil.buildRenderersFactory(this, false);
        downloadTracker = DemoUtil.getDownloadTracker(this);
        downloadTracker.addListener(this);
        mOfflineDelegate = new OfflineDelegateImpl(getApplicationContext());

        updateContentStatus();

        try {
            DownloadService.start(this, DemoDownloadService.class);
        } catch (IllegalStateException e) {
            DownloadService.startForeground(this, DemoDownloadService.class);
        }

        findViewById(R.id.online_sideloaded).setOnClickListener(v -> {
            mCurrentItem = mOnlineSideloaded;
            updateContentStatus();
        });
        findViewById(R.id.offline_sideloaded).setOnClickListener(v -> {
            mCurrentItem = mOfflineSideloaded;
            updateContentStatus();
        });
        findViewById(R.id.in_manifest).setOnClickListener(v -> {
            mCurrentItem = mInManifest;
            updateContentStatus();
        });
        findViewById(R.id.prepare_download).setOnClickListener(v -> {
            if (!mNetworkTracker.isOnline()) {
                Toast.makeText(this, "Offline, cannot download content", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean success = downloadTracker.addDownload(
                    getSupportFragmentManager(),
                    ExoConverter.toMediaItem(mCurrentItem),
                    mRenderersFactory
            );
            if (!success) {
                Toast.makeText(this,
                               "Content already downloaded or in progress",
                               Toast.LENGTH_SHORT
                ).show();
            }
        });
        findViewById(R.id.remove_download).setOnClickListener(v -> {
            boolean success = downloadTracker.removeDownload(mCurrentItem.getMediaId());
            Toast.makeText(this,
                           success ? "Content download removed" : "No content to be removed",
                           Toast.LENGTH_SHORT
            ).show();
            updateContentStatus();
        });
        findViewById(R.id.setup).setOnClickListener(v -> {
           if (mPlayer != null) {
               mPlayer.stop();
           }

            Download download = mOfflineDelegate.getDownload(mCurrentItem.getMediaId());
            boolean isDownloaded = download != null && download.state == Download.STATE_COMPLETED;
            if (mNetworkTracker.isOnline() || isDownloaded) {
                String message = mNetworkTracker.isOnline() ? "Online" : "Offline";
                message += ", ";
                message += isDownloaded ? "Using downloaded content" : "Not Downloaded";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                mPlayerView.setVisibility(View.VISIBLE);

                // Pass in your OfflineDelegate Implementation
                mPlayer = mPlayerView.getPlayer(this, mOfflineDelegate);

                mPlayer.setup(new PlayerConfig.Builder()
                                      .playlist(new ArrayList<PlaylistItem>() {{
                                          add(mCurrentItem);
                                      }})
                                      .build()
                );
            } else {
                mPlayerView.setVisibility(View.GONE);
                Toast.makeText(this, "Offline, no downloaded content", Toast.LENGTH_SHORT)
                     .show();
            }
        });
    }

    private void updateContentStatus() {
        Download download = mOfflineDelegate.getDownload(mCurrentItem.getMediaId());
        boolean isDownloaded = download != null && download.state == Download.STATE_COMPLETED;
        mContentStatus.setText(mCurrentItem.getTitle() + ": " + (isDownloaded ? "Downloaded" : "Not Downloaded"));
    }


    @Override
    public void onNetworkStatusChanged(boolean isOnline) {
        mNetworkStatus.setText("Network Status: " + (isOnline ? "Online" : "Offline"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNetworkTracker.destroy();
    }

    @Override
    public void onDownloadsChanged() {
        updateContentStatus();
    }
}