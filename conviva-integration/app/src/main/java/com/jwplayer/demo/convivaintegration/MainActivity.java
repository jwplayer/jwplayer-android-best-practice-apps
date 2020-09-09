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

import com.jwplayer.demo.R;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;


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

        mPlayerView = findViewById(R.id.jwplayer);
        TextView outputTextView = findViewById(R.id.output);

        mCoordinatorLayout = findViewById(R.id.activity_jwplayerview);

        // Handle hiding/showing of ActionBar
        mPlayerView.addOnFullscreenListener(this);

        // Keep the screen on during playback
        new KeepScreenOnHandler(mPlayerView, getWindow());

        // Instantiate the JW Player event handler class
        mEventHandler = new JWEventHandler(mPlayerView, outputTextView);

        ConvivaSessionManager.initClient(this, CONVIVA_GATEWAY_URL, CONVIVA_CUSTOMER_KEY);

        mPlayerView.addOnPlaylistItemListener(playlistItemEvent ->
                ConvivaSessionManager.createConvivaSession(playlistItemEvent.getPlaylistItem()));

        // Load a media source
        PlaylistItem pi = new PlaylistItem.Builder()
                .file("http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8")
                .title("BipBop")
                .description("A video player testing video.")
                .build();

        mPlayerView.load(pi);

        try {
            mPlayerInterface = new CVJWPlayerInterface(ConvivaSessionManager.getPlayerStateManager(), mPlayerView);
        } catch (Exception e) {
            Log.d(TAG, "CVJWPlayerInterface instance failed");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mPlayerView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayerView.onPause();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            releaseConviva();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlayerView.onStop();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            releaseConviva();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerView.onDestroy();

        ConvivaSessionManager.deinitClient();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Set fullscreen when the device is rotated to landscape
        mPlayerView.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE,
                true);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Exit fullscreen when the user pressed the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPlayerView.getFullscreen()) {
                mPlayerView.setFullscreen(false, true);
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
