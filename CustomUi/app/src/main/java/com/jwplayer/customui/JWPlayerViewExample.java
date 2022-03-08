package com.jwplayer.customui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.view.JWPlayerView;


public class JWPlayerViewExample extends AppCompatActivity
        implements VideoPlayerEvents.OnFullscreenListener {

    private JWPlayerView mPlayerView;

    private JWPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwplayerview);
        WebView.setWebContentsDebuggingEnabled(true);
        // TODO: Add your license key
        new LicenseUtil().setLicenseKey(this, YOUR_LICENSE_KEY);
        mPlayerView = findViewById(R.id.jwplayer);
        mPlayerView.getPlayerAsync(this, this, jwPlayer -> {
            mPlayer = jwPlayer;
            setupPlayer();
        });

    }

    private void setupPlayer() {
        // Handle hiding/showing of ActionBar
        mPlayer.addListener(EventType.FULLSCREEN, JWPlayerViewExample.this);

        // Keep the screen on during playback
        new KeepScreenOnHandler(mPlayer, getWindow());

        // Load a media source
        PlayerConfig config = new PlayerConfig.Builder()
                .playlistUrl("https://cdn.jwplayer.com/v2/playlists/3jBCQ2MI?format=json")
                .build();
        // You want to call setup before you bind the ViewModels because setup creates the ViewModels
        mPlayer.setup(config);

        // We create a MyControls ViewGroup in which we can control the positioning of the Views
        MyControls controls = new MyControls(new ContextThemeWrapper(this, R.style.AppTheme));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        controls.setLayoutParams(params);
        mPlayerView.addView(controls);
        controls.bind(mPlayer, this);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!mPlayer.isInPictureInPictureMode()) {
            final boolean isFullscreen = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
            mPlayer.setFullscreen(isFullscreen, true);
        }
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
    }
}
