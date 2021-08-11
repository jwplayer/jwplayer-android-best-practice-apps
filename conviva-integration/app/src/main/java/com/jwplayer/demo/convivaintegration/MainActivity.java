package com.jwplayer.demo.convivaintegration;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.jwplayer.demo.BuildConfig;
import com.jwplayer.demo.R;
import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.JWPlayerCompat;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.PlaylistItemEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements VideoPlayerEvents.OnFullscreenListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Conviva gateway URL
     */
    private static final String CONVIVA_GATEWAY_URL = "";

    /**
     * Conviva customer key
     */
    private static final String CONVIVA_CUSTOMER_KEY = "";

    /**
     * Reference to the {@link JWPlayerView}
     */
    private JWPlayerView mPlayerView;
    private JWPlayer mPlayer;

    /**
     * An instance of our event handling class
     */
    private JWEventHandler mEventHandler;

    /**
     * Stored instance of CoordinatorLayout
     * http://developer.android.com/reference/android/support/design/widget/CoordinatorLayout.html
     */
    private CoordinatorLayout mCoordinatorLayout;

    private CVJWPlayerInterface mPlayerInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwplayerview);

        // INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
		// [OR] change in app-level build.gradle
		// [OR] set JWPLAYER_LICENSE_KEY as environment variable
		LicenseUtil.setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

        mPlayerView = findViewById(R.id.jwplayer);
        TextView outputTextView = findViewById(R.id.output);

        mCoordinatorLayout = findViewById(R.id.activity_jwplayerview);

        mPlayer = mPlayerView.getPlayer();

        // Handle hiding/showing of ActionBar
        mPlayer.addListener(EventType.FULLSCREEN, this);

        // Keep the screen on during playback
        new KeepScreenOnHandler(mPlayer, getWindow());

        // Instantiate the JW Player event handler class
        mEventHandler = new JWEventHandler(mPlayer, outputTextView);

        ConvivaSessionManager.initClient(this, CONVIVA_GATEWAY_URL, CONVIVA_CUSTOMER_KEY);

        JWPlayerCompat playerCompat = new JWPlayerCompat(mPlayer);
        playerCompat.addOnPlaylistItemListener(playlistItemEvent -> ConvivaSessionManager.createConvivaSession(playlistItemEvent.getPlaylistItem()));

        // Load a media source
        PlaylistItem pi = new PlaylistItem.Builder()
                .file("http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8")
                .title("BipBop")
                .description("A video player testing video.")
                .build();

        List<PlaylistItem> playlist = new ArrayList<>();
        playlist.add(pi);

        PlayerConfig playerConfig = new PlayerConfig.Builder()
                .playlist(playlist)
                .build();

        mPlayer.setup(playerConfig);

        try {
            mPlayerInterface = new CVJWPlayerInterface(ConvivaSessionManager.getPlayerStateManager(), mPlayer);
        } catch (Exception e) {
            Log.d(TAG, "CVJWPlayerInterface instance failed");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            releaseConviva();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            releaseConviva();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConvivaSessionManager.deinitClient();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Set fullscreen when the device is rotated to landscape
        mPlayer.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE,
                true);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Exit fullscreen when the user pressed the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPlayer.getFullscreen()) {
                mPlayer.setFullscreen(false, true);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFullscreen(FullscreenEvent fullscreenEvent) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (fullscreenEvent.getFullscreen()) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
        }

        // When going to Fullscreen we want to set fitsSystemWindows="false"
        mCoordinatorLayout.setFitsSystemWindows(!fullscreenEvent.getFullscreen());
    }

    private void releaseConviva() {

        if (mPlayerInterface != null) {
            mPlayerInterface.cleanup();
            mPlayerInterface = null;
        }
        ConvivaSessionManager.releasePlayerStateManager();
        ConvivaSessionManager.cleanupConvivaSession();
    }
}
